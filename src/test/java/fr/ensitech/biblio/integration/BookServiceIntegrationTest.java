package fr.ensitech.biblio.integration;

import fr.ensitech.biblio.entity.Author;
import fr.ensitech.biblio.entity.Book;
import fr.ensitech.biblio.repository.IBookRepository;
import fr.ensitech.biblio.service.BookService;
import fr.ensitech.biblio.utils.Dates;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookServiceIntegrationTest {

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

    @Test
    @DisplayName("Ajout d'un livre dans la base de données")
    void shouldAddBookToDatabase() throws Exception {
        Book saved = bookService.addOrUpdateBook(book);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isGreaterThan(0);

        assertThat(bookRepository.findById(saved.getId())).isPresent();

        assertThat(saved.getAuthors()).isNotNull();
        assertThat(saved.getAuthors()).hasSize(2);
    }

    @Test
    @DisplayName("Mettre à jour un livre dans la base de données")
    void shouldUpdateBookInDatabase() throws Exception {
        Book saved = bookService.addOrUpdateBook(book);

        saved.setNbPages((short) 200);
        saved.setLanguage("IT");
        saved.setCategory("XJAVAX");

        Book updated = bookService.addOrUpdateBook(saved);

        assertThat(updated.getNbPages()).isEqualTo((short) 200);
        assertThat(updated.getLanguage()).isEqualTo("IT");
        assertThat(updated.getCategory()).isEqualTo("XJAVAX");

        Book reloaded = bookRepository.findById(updated.getId()).orElseThrow();
        assertThat(reloaded.getNbPages()).isEqualTo((short) 200);
        assertThat(reloaded.getLanguage()).isEqualTo("IT");
        assertThat(reloaded.getCategory()).isEqualTo("XJAVAX");
    }

    @Test
    @DisplayName("Refuser l'ajout si ISBN déjà existant")
    void shouldThrowWhenSavingBookWithAlreadyExistingIsbn() throws Exception {
        bookService.addOrUpdateBook(book);

        Book another = Book.builder()
                .title("Autre")
                .description("Autre desc")
                .isbn("1234567890") // même isbn
                .category("TEST")
                .published(true)
                .publicationDate(Dates.convertStringToDate("01/01/2010"))
                .editor("X")
                .nbPages((short) 10)
                .language("FR")
                .author(Author.builder().firstname("A").lastname("B").build())
                .build();

        assertThatThrownBy(() -> bookService.addOrUpdateBook(another))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book with same ISBN already exists");
    }

    @Test
    @DisplayName("getBooks() doit retourner au moins 1 livre après insertion")
    void shouldGetBooks() throws Exception {
        bookService.addOrUpdateBook(book);

        List<Book> books = bookService.getBooks();

        assertThat(books).isNotNull();
        assertThat(books).isNotEmpty();
    }

    @Test
    @DisplayName("getBook(id) doit retourner le livre")
    void shouldGetBookById() throws Exception {
        Book saved = bookService.addOrUpdateBook(book);

        Book found = bookService.getBook(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getIsbn()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("getBooksByTitle(title) doit retourner la liste correspondante")
    void shouldGetBooksByTitle() throws Exception {
        bookService.addOrUpdateBook(book);

        List<Book> result = bookService.getBooksByTitle("JAVAAAAAAAH");

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getTitle()).isEqualTo("JAVAAAAAAAH");
    }

    @Test
    @DisplayName("getBooksByPublished(published) doit filtrer")
    void shouldGetBooksByPublished() throws Exception {
        bookService.addOrUpdateBook(book);

        List<Book> publishedBooks = bookService.getBooksByPublished(true);
        List<Book> nonPublishedBooks = bookService.getBooksByPublished(false);

        assertThat(publishedBooks).isNotEmpty();
        assertThat(nonPublishedBooks).isEmpty();
    }

    @Test
    @DisplayName("getBookByIsbn(isbn) doit retourner le livre")
    void shouldGetBookByIsbn() throws Exception {
        bookService.addOrUpdateBook(book);

        Book found = bookService.getBookByIsbn("1234567890");

        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("JAVAAAAAAAH");
    }

    @Test
    @DisplayName("getBooksByTitleOrDescription(title, description) doit matcher")
    void shouldGetBooksByTitleOrDescription() throws Exception {
        bookService.addOrUpdateBook(book);

        List<Book> foundByTitle = bookService.getBooksByTitleOrDescription("java", "xxxxx");
        List<Book> foundByDesc = bookService.getBooksByTitleOrDescription("xxxxx", "cours");

        assertThat(foundByTitle).isNotEmpty();
        assertThat(foundByDesc).isNotEmpty();
    }

    @Test
    @DisplayName("getBooksBetweenYears(start, end) doit retourner les livres dans la période")
    void shouldGetBooksBetweenYears() throws Exception {
        bookService.addOrUpdateBook(book); // 2000

        List<Book> inRange = bookService.getBooksBetweenYears(1999, 2001);
        List<Book> outRange = bookService.getBooksBetweenYears(2010, 2012);

        assertThat(inRange).isNotEmpty();
        assertThat(outRange).isEmpty();
    }

    @Test
    @DisplayName("deleteBook(id) doit supprimer")
    void shouldDeleteExistingBook() throws Exception {
        Book saved = bookService.addOrUpdateBook(book);

        bookService.deleteBook(saved.getId());

        assertThat(bookRepository.findById(saved.getId())).isEmpty();
    }
}
