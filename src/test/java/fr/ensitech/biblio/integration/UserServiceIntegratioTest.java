package fr.ensitech.biblio.integration;

import fr.ensitech.biblio.entity.SecurityQuestion;
import fr.ensitech.biblio.entity.User;
import fr.ensitech.biblio.repository.ISecurityQuestionRepository;
import fr.ensitech.biblio.repository.IUserRepository;
import fr.ensitech.biblio.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired private UserService userService;
    @Autowired private IUserRepository userRepository;
    @Autowired private ISecurityQuestionRepository securityQuestionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private SecurityQuestion q;
    private User user;

    @BeforeEach
    void setUp() {
        // On crée une question en base pour être sûr que l'ID existe
        q = new SecurityQuestion();
        q.setQuestion("Quel est ton animal préféré ?");
        q = securityQuestionRepository.save(q);

        user = new User();
        user.setFirstname("Lucas");
        user.setLastname("Test");
        user.setEmail("lucas@test.com");
        user.setPassword("1234");
        user.setRole("U");
        user.setBirthdate(new Date());

        user.setSecurityQuestionId(q.getId());
        user.setSecurityAnswer("Minou");
    }

    @Test
    @DisplayName("register() doit hasher password + réponse secrète et créer user inactif")
    void shouldRegisterAndHash() throws Exception {
        userService.register(user);

        User saved = userRepository.findByEmail("lucas@test.com");
        assertThat(saved).isNotNull();
        assertThat(saved.isActive()).isFalse();

        assertThat(passwordEncoder.matches("1234", saved.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("minou", saved.getSecurityAnswerHash())).isTrue();

        assertThat(saved.getSecurityQuestion()).isNotNull();
        assertThat(saved.getSecurityQuestion().getId()).isEqualTo(q.getId());
        assertThat(saved.getLastPasswordChange()).isNotNull();
    }

    @Test
    @DisplayName("activateAccount() doit activer")
    void shouldActivateAccount() throws Exception {
        userService.register(user);

        userService.activateAccount("lucas@test.com");

        User saved = userRepository.findByEmail("lucas@test.com");
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    @DisplayName("login() doit réussir si actif + password OK")
    void shouldLoginSuccess() throws Exception {
        userService.register(user);
        userService.activateAccount("lucas@test.com");

        User logged = userService.login("lucas@test.com", "1234");

        assertThat(logged).isNotNull();
        assertThat(logged.getEmail()).isEqualTo("lucas@test.com");
    }

    @Test
    @DisplayName("login() doit échouer si non activé")
    void shouldFailLoginIfNotActivated() throws Exception {
        userService.register(user);

        assertThatThrownBy(() -> userService.login("lucas@test.com", "1234"))
                .isInstanceOf(Exception.class)
                .hasMessage("Compte non activé");
    }

    @Test
    @DisplayName("login() doit échouer si password incorrect")
    void shouldFailLoginIfWrongPassword() throws Exception {
        userService.register(user);
        userService.activateAccount("lucas@test.com");

        assertThatThrownBy(() -> userService.login("lucas@test.com", "BAD"))
                .isInstanceOf(Exception.class)
                .hasMessage("Identifiants invalides");
    }

    @Test
    @DisplayName("updatePassword(id, old, new) doit changer le hash")
    void shouldUpdatePassword() throws Exception {
        userService.register(user);
        userService.activateAccount("lucas@test.com");

        User saved = userRepository.findByEmail("lucas@test.com");
        String oldHash = saved.getPassword();

        userService.updatePassword(saved.getId(), "1234", "newPwd");

        User updated = userRepository.findByEmail("lucas@test.com");
        assertThat(updated.getPassword()).isNotEqualTo(oldHash);
        assertThat(passwordEncoder.matches("newPwd", updated.getPassword())).isTrue();
    }

    @Test
    @DisplayName("updateProfile(id) doit modifier uniquement les champs autorisés")
    void shouldUpdateProfile() throws Exception {
        userService.register(user);
        User saved = userRepository.findByEmail("lucas@test.com");

        User patch = new User();
        patch.setFirstname("Lucas2");
        patch.setLastname("Test2");
        patch.setRole("A");
        patch.setBirthdate(new Date(0));
        patch.setActive(true);

        // tentatives interdites
        patch.setEmail("hacker@test.com");
        patch.setPassword("CLEAR");

        userService.updateProfile(saved.getId(), patch);

        User updated = userRepository.findByEmail("lucas@test.com");
        assertThat(updated.getFirstname()).isEqualTo("Lucas2");
        assertThat(updated.getLastname()).isEqualTo("Test2");
        assertThat(updated.getRole()).isEqualTo("A");
        assertThat(updated.isActive()).isTrue();

        // email/password inchangés
        assertThat(updated.getEmail()).isEqualTo("lucas@test.com");
        assertThat(passwordEncoder.matches("1234", updated.getPassword())).isTrue();
    }

    @Test
    @DisplayName("unsubscribe(email) doit supprimer")
    void shouldUnsubscribe() throws Exception {
        userService.register(user);

        userService.unsubscribe("lucas@test.com");

        assertThat(userRepository.findByEmail("lucas@test.com")).isNull();
    }
}
