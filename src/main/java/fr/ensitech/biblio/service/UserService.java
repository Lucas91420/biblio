package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.PasswordHistory;
import fr.ensitech.biblio.entity.SecurityQuestion;
import fr.ensitech.biblio.entity.User;
import fr.ensitech.biblio.repository.IPasswordHistoryRepository;
import fr.ensitech.biblio.repository.ISecurityQuestionRepository;
import fr.ensitech.biblio.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ISecurityQuestionRepository securityQuestionRepository;

    @Autowired
    private IPasswordHistoryRepository passwordHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // BCrypt

    @Override
    public void createUser(User user)throws Exception{
        userRepository.save(user);
    }

    @Override
    public User getUserById(long id)throws Exception{
        Optional<User> optional = userRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public List<User> getUserByBirthdate(Date dateInf, Date dateSup) throws Exception {
        return userRepository.findByBirthdateBetween(dateInf, dateSup);
    }

    // ==========================
    //  INSCRIPTION (BCrypt + question secrète)
    // ==========================
    @Override
    public void register(User user) throws Exception {
        User existing = userRepository.findByEmail(user.getEmail());
        if (existing != null) {
            throw new Exception("Email deja existant");
        }

        if (user.getSecurityQuestionId() == null) {
            throw new Exception("Question de sécurité obligatoire");
        }
        if (user.getSecurityAnswer() == null || user.getSecurityAnswer().isBlank()) {
            throw new Exception("Réponse de sécurité obligatoire");
        }

        SecurityQuestion question = securityQuestionRepository
                .findById(user.getSecurityQuestionId())
                .orElseThrow(() -> new Exception("Question de sécurité invalide"));

        // Hash du mot de passe
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        // Hash de la réponse (normalisée pour éviter les problèmes de casse / espaces)
        String normalizedAnswer = normalizeSecurityAnswer(user.getSecurityAnswer());
        String hashedAnswer = passwordEncoder.encode(normalizedAnswer);
        user.setSecurityAnswerHash(hashedAnswer);

        user.setSecurityQuestion(question);
        user.setActive(false);
        user.setLastPasswordChange(new Date());

        // On ne stocke pas les champs transient
        user.setSecurityQuestionId(null);
        user.setSecurityAnswer(null);

        userRepository.save(user);
    }

    // ==========================
    //  ACTIVATION COMPTE
    // ==========================
    @Override
    public void activateAccount(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("Email non trouvé");
        }
        user.setActive(true);
        userRepository.save(user);
    }

    // ==========================
    //  LOGIN (étape 1 : email + password)
    // ==========================
    @Override
    public User login(String email, String password) throws Exception {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new Exception("Identifiants invalides");
        }
        if (!user.isActive()) {
            throw new Exception("Compte non activé");
        }

        // Vérifier expiration du mot de passe
        if (isPasswordExpired(user.getLastPasswordChange())) {
            throw new Exception("Mot de passe expiré");
        }

        // vérification du mot de passe (hashé)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Identifiants invalides");
        }

        // Etape 1 OK : on retournera la question côté controller
        return user;
    }

    // ==========================
    //  DOUBLE AUTH : vérif réponse secrète
    // ==========================
    @Override
    public void verifySecurityAnswer(String email, String answer) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("Authentification échouée");
        }

        if (user.getSecurityQuestion() == null || user.getSecurityAnswerHash() == null) {
            throw new Exception("Question de sécurité non définie");
        }

        String normalized = normalizeSecurityAnswer(answer);
        boolean ok = passwordEncoder.matches(normalized, user.getSecurityAnswerHash());
        if (!ok) {
            throw new Exception("Réponse incorrecte");
        }
    }

    // ==========================
    //  DESINSCRIPTION
    // ==========================
    @Override
    public void unsubscribe(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("Email non trouvé");
        }
        userRepository.delete(user);
    }

    // ==========================
    //  UPDATE MOT DE PASSE (par id)
    // ==========================
    @Override
    public void updatePassword(long id, String oldPassword, String newPassword) throws Exception {
        if (id <= 0) {
            throw new Exception("Id invalide");
        }
        if (oldPassword == null || oldPassword.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            throw new Exception("Mot de passe invalide");
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new Exception("Utilisateur non trouvé");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new Exception("Ancien mot de passe incorrect");
        }

        // Vérifier contre le courant + historique (5 derniers)
        checkPasswordNotInHistoryOrCurrent(user, newPassword);

        // Sauvegarder l'ancien dans l'historique
        savePasswordInHistory(user, user.getPassword());

        // Nouveau hash
        String hashedNewPwd = passwordEncoder.encode(newPassword);
        user.setPassword(hashedNewPwd);
        user.setLastPasswordChange(new Date());

        userRepository.save(user);
    }

    // ==========================
    //  UPDATE PROFIL (tout sauf email + password)
    // ==========================
    @Override
    public void updateProfile(long id, User updatedUser) throws Exception {
        if (id <= 0) {
            throw new Exception("Id invalide");
        }
        if (updatedUser == null) {
            throw new Exception("Données profil invalides");
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new Exception("Utilisateur non trouvé");
        }

        // firstname
        if (updatedUser.getFirstname() != null && !updatedUser.getFirstname().isBlank()) {
            user.setFirstname(updatedUser.getFirstname());
        }

        // lastname
        if (updatedUser.getLastname() != null && !updatedUser.getLastname().isBlank()) {
            user.setLastname(updatedUser.getLastname());
        }

        // role
        if (updatedUser.getRole() != null && !updatedUser.getRole().isBlank()) {
            user.setRole(updatedUser.getRole());
        }

        // birthdate
        user.setBirthdate(updatedUser.getBirthdate());

        // active
        user.setActive(updatedUser.isActive());


        userRepository.save(user);
    }

    // ==========================
    //  RENOUVELLEMENT MOT DE PASSE PAR EMAIL
    //  /api/users/{email}/password/renew
    // ==========================
    @Override
    public void renewPassword(String email, String oldPassword, String newPassword) throws Exception {
        if (email == null || email.isBlank()) {
            throw new Exception("Email invalide");
        }
        if (oldPassword == null || oldPassword.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            throw new Exception("Mot de passe invalide");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("Utilisateur non trouvé");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new Exception("Ancien mot de passe incorrect");
        }

        // Vérifier contre le courant + historique
        checkPasswordNotInHistoryOrCurrent(user, newPassword);

        // sauvegarder ancien mot de passe dans l'historique
        savePasswordInHistory(user, user.getPassword());

        // hash nouveau
        String hashedNewPwd = passwordEncoder.encode(newPassword);
        user.setPassword(hashedNewPwd);
        user.setLastPasswordChange(new Date());

        userRepository.save(user);
    }

    // ==========================
    //  PRIVATE HELPERS
    // ==========================
    private boolean isPasswordExpired(Date lastChange) {
        if (lastChange == null) {
            return true; // on force le renouvellement si aucune date
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastChange);
        cal.add(Calendar.WEEK_OF_YEAR, 12); // +12 semaines

        Date expiry = cal.getTime();
        return new Date().after(expiry);
    }

    private String normalizeSecurityAnswer(String answer) {
        return answer.trim().toLowerCase();
    }

    private void savePasswordInHistory(User user, String passwordHash) {
        PasswordHistory ph = new PasswordHistory();
        ph.setUser(user);
        ph.setPasswordHash(passwordHash);
        ph.setChangeDate(new Date());
        passwordHistoryRepository.save(ph);

        // ne garder que les 5 derniers
        List<PasswordHistory> history = passwordHistoryRepository.findByUserOrderByChangeDateDesc(user);
        if (history.size() > 5) {
            List<PasswordHistory> toDelete = history.subList(5, history.size());
            passwordHistoryRepository.deleteAll(toDelete);
        }
    }

    private void checkPasswordNotInHistoryOrCurrent(User user, String newPassword) throws Exception {
        // Ne doit pas égaler le mot de passe courant
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new Exception("Le nouveau mot de passe doit être différent de l'actuel");
        }

        List<PasswordHistory> history = passwordHistoryRepository.findByUserOrderByChangeDateDesc(user);
        for (PasswordHistory ph : history) {
            if (passwordEncoder.matches(newPassword, ph.getPasswordHash())) {
                throw new Exception("Le nouveau mot de passe ne doit pas faire partie des 5 derniers");
            }
        }
    }
}
