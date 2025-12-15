package fr.ensitech.biblio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "reservations", catalog = "biblio_database")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "reservation_date", nullable = false)
    private Date reservationDate;

    @Column(nullable = false)
    private boolean active;
}
