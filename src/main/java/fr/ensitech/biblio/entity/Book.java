package fr.ensitech.biblio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books", catalog = "biblio_database")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(length = 1024)
    private String description;

    @Column(nullable = false)
    private boolean published;

    @Column(length = 128)
    private String editor;

    @Temporal(TemporalType.DATE)
    @Column(name = "publication_date")
    private Date publicationDate;

    @Column(nullable = false, unique = true, length = 32)
    private String isbn;

    @Column(name = "nb_pages")
    private short nbPages;

    @Column(length = 64)
    private String category;

    @Column(length = 64)
    private String language;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    // Relation vers Author
    @ManyToMany (fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "author_book", joinColumns = @JoinColumn(name = "author_id"), inverseJoinColumns =  @JoinColumn(name = "book_id"))
    @Singular
    private Set<Author> authors = new HashSet<>();
}
