package fr.ensitech.biblio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "password_history", catalog = "biblio_database")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class PasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "change_date", nullable = false)
    private Date changeDate;
}
