package fr.ensitech.biblio.integration;

import fr.ensitech.biblio.entity.Author;
import fr.ensitech.biblio.entity.Book;
import fr.ensitech.biblio.repository.IBookRepository;
import fr.ensitech.biblio.service.BookService;
import fr.ensitech.biblio.utils.Dates;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for BookService.
 * This class is currently empty and serves as a placeholder for future integration tests.
 */

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BookServiceIntegrationTest {

    @Autowired
    private IBookRepository bookRepository;

    @Autowired
    private BookService bookService;

    private Book book;
    private Author author1;
    private Author author2;

    @SneakyThrows
    @BeforeEach
    void setUp() throws Exception {

        author1 = Author.builder()
                .firstname("John")
                .lastname("Doe")
                .build();

        author2 = Author.builder()
                .firstname("Pip")
                .lastname("Lil")
                .build();


        book = Book.builder()
                .title("JAVAAAAAAAH")
                .description("JAAAVAAVAAVAVAVAVAAAA")
                .isbn("1234567890")
                .category("JAVAHAHAHA")
                .published(true)
                .publicationDate(Dates.convertStringToDate("15/03/2000"))
                .editor("JAVAX")
                .nbPages((short) 155)
                .language("EN")
                .author(author1)
                .author(author2)
                .build();
    }

    @AfterEach
    void tearDown() {

    }

    @SneakyThrows
    @Test
    @DisplayName("Ajoute d'un livre dans la base de données")
    void shouldAddBookToDatabase() {
        //GIVEN
        //SetUp

        //WHEN
        Book savedbook = bookService.addOrUpdateBook(book);
        //THEN
        assertThat(savedbook).isNotNull();
        assertThat(savedbook.getId()).isGreaterThan(0);
        assertThat(bookRepository.findById(savedbook.getId())).isPresent();
        // verifier les auteurs
        assertThat(savedbook.getAuthors()).isNotNull();
        assertThat(savedbook.getAuthors().size()).isEqualTo(2);
    }

    @SneakyThrows
    @Test
    @DisplayName("Mettre à jour un livre dans la base de données")
    void shouldUpdateBookInDatabase() {

        //GIVEN
        Book _book = bookService.addOrUpdateBook(book);
        _book.setNbPages((short)200);
        _book.setLanguage("IT");
        _book.setCategory("XJAVAX");

        //WHEN
        Book updatedBook = bookService.addOrUpdateBook(_book);

        //THEN
        //assertThat(updatedBook).isNotNull();
        //assertThat(updatedBook.getId()).isEqualTo(_book.getId());
        assertThat(updatedBook.getNbPages()).isEqualTo((short)200);
        assertThat(updatedBook.getLanguage()).isEqualTo("IT");
        assertThat(updatedBook.getCategory()).isEqualTo("XJAVAX");
    }

}
