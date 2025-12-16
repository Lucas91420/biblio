package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.Author;
import fr.ensitech.biblio.entity.Book;
import fr.ensitech.biblio.repository.IAuthorRepository;
import fr.ensitech.biblio.repository.IBookRepository;
import fr.ensitech.biblio.utils.Dates;
import lombok.SneakyThrows;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.Option;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IAuthorRepository authorRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private Author author1, author2;

    @SneakyThrows
    @BeforeEach
    void setUp() {

        author1 = Author.builder()
                .id(1L)
                .firstname("Pascal")
                .lastname("LAMBERT")
                .build();

        author2 = Author.builder()
                .id(2L)
                .firstname("Benoit")
                .lastname("DECOUX")
                .build();

        book = Book.builder()
                .id(1L)
                .title("Livre de Java")
                .description("Cours et Exercices en Java")
                .isbn("123456789")
                .editor("Editions Eyrolles")
                .category("Informatique")
                .nbPages((short) 155)
                .language("FR")
                .published(true)
                .publicationDate(Dates.convertStringToDate("15/03/2000"))
                .author(author1)
                .author(author2)
                .build();
    }

    @AfterEach
    void tearDown() {

    }

    @SneakyThrows
    @Test
    @DisplayName("Ajouter un livre valide avec plusieurs auteurs")
    void shouldAddBookSuccessfully() {

        //GIVEN
        book.setId(0L);
        //when(bookRepository.findById(0L)).thenReturn(Optional.ofNullable(book));
        when(bookRepository.save(book)).thenReturn(book);
        when(authorRepository.save(author1)).thenReturn(author1);
        when(authorRepository.save(author2)).thenReturn(author2);
        when(bookRepository.findByIsbnIgnoreCase(book.getIsbn())).thenReturn(null);

        //WHEN
        Book savedBook = bookService.addOrUpdateBook(book);

        //THEN
        assertThat(savedBook)
                .isNotNull()
                .extracting(Book::getTitle, Book::getDescription)
                .containsExactly("Livre de Java", "Cours et Exercices en Java");

        assertThat(savedBook.getAuthors())
                .hasSize(2)
                .extracting(Author::getFirstname)
                .containsExactly("Pascal", "Benoit");

        assertThat(savedBook.getAuthors())
                .extracting(Author::getLastname)
                .containsExactly("LAMBERT", "DECOUX");

        verify(bookRepository).save(book);
    }

    @SneakyThrows
    @Test
    void shouldThrowExceptionWhenSavingBookWithAlreadyExistingIsbn()
    {
        //GIVEN
        //when(bookRepository.save(book)).thenReturn(book);
        //when(bookRepository.findById(1L)).thenReturn(Optional.ofNullable(book));
        book.setId(0L);
        when(bookRepository.findByIsbnIgnoreCase(book.getIsbn())).thenReturn(book);

        //WHEN
        //THEN
        assertThatThrownBy(
                () -> bookService.addOrUpdateBook(book))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book with same ISBN already exists");
    }

    @SneakyThrows
    @Test
    void shouldDeleteExistingBook() {

        //GIVEN
        when(bookRepository.findById(1L)).thenReturn(Optional.ofNullable(book));

        //WHEN
        bookService.deleteBook(1L);

        //THEN
        verify(bookRepository).deleteById(1L);
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenDeletingNoExistingBook() {

        //GIVEN

        when(bookRepository.findById(2L)).thenReturn(Optional.empty());
        //WHEN
        //THEN
        assertThatThrownBy(
                () -> bookService.deleteBook(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book id must be > 0");

        assertThatThrownBy(
                () -> bookService.deleteBook(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book id must be > 0");

        assertThatThrownBy(
                () -> bookService.deleteBook(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book not found !");

    }

//@Test
    void shouldUpdatingExistingBook(){

    }

    //@Test
    void shouldThrowExceptionWhenUpdatingNoExistingBook(){


    }

    //@Test
    void shouldFindBooksByTitle(){

    }
}
