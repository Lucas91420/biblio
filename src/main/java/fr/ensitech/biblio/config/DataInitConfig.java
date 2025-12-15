package fr.ensitech.biblio.config;

import fr.ensitech.biblio.entity.SecurityQuestion;
import fr.ensitech.biblio.repository.ISecurityQuestionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class DataInitConfig {

    @Bean
    public CommandLineRunner initSecurityQuestions(ISecurityQuestionRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.saveAll(Arrays.asList(
                        new SecurityQuestion(null, "Quel est le nom de votre premier animal ?"),
                        new SecurityQuestion(null, "Quel est le nom de jeune fille de votre mère ?"),
                        new SecurityQuestion(null, "Dans quelle ville êtes-vous né ?"),
                        new SecurityQuestion(null, "Quel est le prénom de votre meilleur ami d'enfance ?"),
                        new SecurityQuestion(null, "Quel est votre film préféré ?")
                ));
            }
        };
    }
}
