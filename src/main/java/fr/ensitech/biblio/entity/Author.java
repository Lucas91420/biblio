package fr.ensitech.biblio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authors", catalog = "biblio_database")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 48)
    private String firstname;

    @Column(nullable = false, length = 48)
    private String lastname;

    // Relation vers Book
    @ManyToMany (mappedBy = "authors", fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<Book>();
}
