package fr.ensitech.biblio.repository;

import fr.ensitech.biblio.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface IBookRepository extends JpaRepository<Book, Long> {

    //lister tous les livres publiés 1
    List<Book> findByPublished(boolean published);

    //chercher un livre par son titre 2
    List<Book> findByTitleIgnoreCase(String title);

    //afficher tous les livres dont le titre contient une chaîne 3
    List<Book> findByTitleContainingIgnoreCase(String title);

    //chercher un livre par son isbn 4
    Book findByIsbnIgnoreCase(String isbn);

    //Un livre dont le titre ou la description contient un texte précis 5
    List<Book> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);

    // Recherche tous les livres publiés entre deux dates précise 6
    List<Book> findByPublicationDateBetween(Date startDate, Date endDate);

    //@Query("select b from Book b where  ")
    //List<Book> findBooksByAuthor(Author author);

}
