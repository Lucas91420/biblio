package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.SecurityQuestion;
import fr.ensitech.biblio.entity.User;
import fr.ensitech.biblio.repository.IPasswordHistoryRepository;
import fr.ensitech.biblio.repository.ISecurityQuestionRepository;
import fr.ensitech.biblio.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private IUserRepository userRepository;
    @Mock private ISecurityQuestionRepository securityQuestionRepository;
    @Mock private IPasswordHistoryRepository passwordHistoryRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private SecurityQuestion q1;

    @BeforeEach
    void setUp() {
        q1 = new SecurityQuestion();
        q1.setId(1L);
        q1.setQuestion("Quel est ton animal préféré ?");

        user = new User();
        user.setId(1L);
        user.setFirstname("Lucas");
        user.setLastname("Test");
        user.setEmail("lucas@test.com");
        user.setPassword("1234"); // clair avant hash
        user.setRole("U");
        user.setBirthdate(new Date());
        user.setActive(false);

        user.setSecurityQuestionId(1L);
        user.setSecurityAnswer("Minou");
    }

    // =========================
    // REGISTER
    // =========================

    @Test
    @DisplayName("register() doit hasher le password et la réponse secrète puis sauvegarder")
    void shouldRegisterAndHashPasswordAndAnswer() throws Exception {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(null);
        when(securityQuestionRepository.findById(1L)).thenReturn(Optional.of(q1));

        when(passwordEncoder.encode("1234")).thenReturn("HASHED_PWD");
        when(passwordEncoder.encode("minou")).thenReturn("HASHED_ANS");

        userService.register(user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getPassword()).isEqualTo("HASHED_PWD");
        assertThat(saved.getSecurityAnswerHash()).isEqualTo("HASHED_ANS");
        assertThat(saved.getSecurityQuestion()).isEqualTo(q1);
        assertThat(saved.isActive()).isFalse();
        assertThat(saved.getLastPasswordChange()).isNotNull();

        // transient nettoyés
        assertThat(saved.getSecurityQuestionId()).isNull();
        assertThat(saved.getSecurityAnswer()).isNull();
    }

    @Test
    @DisplayName("register() doit échouer si email existe déjà")
    void shouldThrowWhenEmailAlreadyExists() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(new User());

        assertThatThrownBy(() -> userService.register(user))
                .isInstanceOf(Exception.class)
                .hasMessage("Email deja existant");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register() doit échouer si securityQuestionId manquant")
    void shouldThrowWhenSecurityQuestionMissing() {
        user.setSecurityQuestionId(null);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(null);

        assertThatThrownBy(() -> userService.register(user))
                .isInstanceOf(Exception.class)
                .hasMessage("Question de sécurité obligatoire");
    }

    @Test
    @DisplayName("register() doit échouer si réponse secrète manquante")
    void shouldThrowWhenSecurityAnswerMissing() {
        user.setSecurityAnswer("   ");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(null);

        assertThatThrownBy(() -> userService.register(user))
                .isInstanceOf(Exception.class)
                .hasMessage("Réponse de sécurité obligatoire");
    }

    @Test
    @DisplayName("register() doit échouer si question de sécurité invalide")
    void shouldThrowWhenSecurityQuestionInvalid() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(null);
        when(securityQuestionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.register(user))
                .isInstanceOf(Exception.class)
                .hasMessage("Question de sécurité invalide");
    }

    // =========================
    // ACTIVATE
    // =========================

    @Test
    @DisplayName("activateAccount() doit activer le user")
    void shouldActivateAccount() throws Exception {
        when(userRepository.findByEmail("lucas@test.com")).thenReturn(user);

        userService.activateAccount("lucas@test.com");

        assertThat(user.isActive()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("activateAccount() doit échouer si email inconnu")
    void shouldThrowWhenActivateEmailNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(null);

        assertThatThrownBy(() -> userService.activateAccount("unknown@test.com"))
                .isInstanceOf(Exception.class)
                .hasMessage("Email non trouvé");
    }

    // =========================
    // LOGIN
    // =========================

    @Test
    @DisplayName("login() succès si actif, mdp OK, non expiré")
    void shouldLoginSuccess() throws Exception {
        user.setActive(true);
        user.setPassword("HASHED_PWD");
        user.setLastPasswordChange(new Date());

        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(passwordEncoder.matches("1234", "HASHED_PWD")).thenReturn(true);

        User logged = userService.login(user.getEmail(), "1234");

        assertThat(logged).isNotNull();
        assertThat(logged.getEmail()).isEqualTo("lucas@test.com");
    }

    @Test
    @DisplayName("login() doit échouer si compte non activé")
    void shouldThrowWhenAccountNotActivated() {
        user.setActive(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        assertThatThrownBy(() -> userService.login(user.getEmail(), "1234"))
                .isInstanceOf(Exception.class)
                .hasMessage("Compte non activé");
    }

    @Test
    @DisplayName("login() doit échouer si mot de passe incorrect")
    void shouldThrowWhenPasswordWrong() {
        user.setActive(true);
        user.setPassword("HASHED_PWD");
        user.setLastPasswordChange(new Date());

        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(passwordEncoder.matches("BAD", "HASHED_PWD")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(user.getEmail(), "BAD"))
                .isInstanceOf(Exception.class)
                .hasMessage("Identifiants invalides");
    }

    @Test
    @DisplayName("login() doit échouer si email inconnu")
    void shouldThrowWhenLoginEmailUnknown() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(null);

        assertThatThrownBy(() -> userService.login("unknown@test.com", "1234"))
                .isInstanceOf(Exception.class)
                .hasMessage("Identifiants invalides");
    }

    // =========================
    // UPDATE PASSWORD (id)
    // =========================

    @Test
    @DisplayName("updatePassword() succès si ancien ok et nouveau différent + historique ok")
    void shouldUpdatePasswordSuccess() throws Exception {
        user.setPassword("HASHED_OLD");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("old", "HASHED_OLD")).thenReturn(true);
        when(passwordEncoder.matches("new", "HASHED_OLD")).thenReturn(false);

        when(passwordHistoryRepository.findByUserOrderByChangeDateDesc(user))
                .thenReturn(java.util.List.of());

        when(passwordEncoder.encode("new")).thenReturn("HASHED_NEW");

        userService.updatePassword(1L, "old", "new");

        assertThat(user.getPassword()).isEqualTo("HASHED_NEW");
        assertThat(user.getLastPasswordChange()).isNotNull();

        verify(userRepository).save(user);
        verify(passwordHistoryRepository).save(any());
    }

    @Test
    @DisplayName("updatePassword() doit échouer si ancien mot de passe incorrect")
    void shouldThrowWhenOldPasswordIncorrect() {
        user.setPassword("HASHED_OLD");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "HASHED_OLD")).thenReturn(false);

        assertThatThrownBy(() -> userService.updatePassword(1L, "old", "new"))
                .isInstanceOf(Exception.class)
                .hasMessage("Ancien mot de passe incorrect");

        verify(userRepository, never()).save(any());
    }

    // =========================
    // UPDATE PROFILE
    // =========================

    @Test
    @DisplayName("updateProfile() doit modifier les champs autorisés et ne pas toucher email/password")
    void shouldUpdateProfileButNotEmailOrPassword() throws Exception {
        user.setPassword("HASHED_PWD");
        user.setEmail("lucas@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User updated = new User();
        updated.setFirstname("Lucas2");
        updated.setLastname("Test2");
        updated.setRole("A");
        updated.setBirthdate(new Date(0));
        updated.setActive(true);

        // tentatives interdites
        updated.setEmail("hacker@test.com");
        updated.setPassword("CLEAR");

        userService.updateProfile(1L, updated);

        assertThat(user.getFirstname()).isEqualTo("Lucas2");
        assertThat(user.getLastname()).isEqualTo("Test2");
        assertThat(user.getRole()).isEqualTo("A");
        assertThat(user.isActive()).isTrue();

        // email/password inchangés
        assertThat(user.getEmail()).isEqualTo("lucas@test.com");
        assertThat(user.getPassword()).isEqualTo("HASHED_PWD");

        verify(userRepository).save(user);
    }

    // =========================
    // UNSUBSCRIBE
    // =========================

    @Test
    @DisplayName("unsubscribe() doit supprimer l'utilisateur")
    void shouldUnsubscribe() throws Exception {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        userService.unsubscribe(user.getEmail());

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("unsubscribe() doit échouer si email introuvable")
    void shouldThrowWhenUnsubscribeEmailNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(null);

        assertThatThrownBy(() -> userService.unsubscribe(user.getEmail()))
                .isInstanceOf(Exception.class)
                .hasMessage("Email non trouvé");

        verify(userRepository, never()).delete(any());
    }
}
