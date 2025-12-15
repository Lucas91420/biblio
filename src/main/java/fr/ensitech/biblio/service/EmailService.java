package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendActivationEmail(User user) {
        String subject = "Activation de votre compte Biblio";
        String activationLink = "http://localhost:8080/api/users/activate?email=" + user.getEmail();

        String text = "Bonjour " + user.getFirstname() + ",\n\n"
                + "Merci pour votre inscription sur Biblio.\n"
                + "Veuillez activer votre compte en cliquant sur le lien suivant :\n"
                + activationLink + "\n\n"
                + "À bientôt.";

        sendEmail(user.getEmail(), subject, text);
    }

    public void sendUnsubscribeConfirmation(User user) {
        String subject = "Confirmation de désinscription";
        String text = "Bonjour " + user.getFirstname() + ",\n\n"
                + "Votre compte a bien été supprimé.\n"
                + "Merci d'avoir utilisé Biblio.\n\n"
                + "Cordialement.";

        sendEmail(user.getEmail(), subject, text);
    }

    public void sendActivationNotification(User user) {
        String subject = "Votre compte est maintenant actif";
        String text = "Bonjour " + user.getFirstname() + ",\n\n"
                + "Votre compte Biblio est désormais actif.\n"
                + "Vous pouvez maintenant vous connecter.\n\n"
                + "Bonne journée !";

        sendEmail(user.getEmail(), subject, text);
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
