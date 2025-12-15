package fr.ensitech.biblio.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "security_questions", catalog = "biblio_database")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class SecurityQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String label;
}
