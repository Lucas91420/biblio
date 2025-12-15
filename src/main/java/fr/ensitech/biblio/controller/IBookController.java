package fr.ensitech.biblio.controller;

import fr.ensitech.biblio.entity.Book;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IBookController {

    ResponseEntity<Book> createBook(Book book);
    ResponseEntity<Book> getBookById(long id);
    ResponseEntity<Book> updateBook(Book book);
    ResponseEntity<String> deleteBook(long id);
    ResponseEntity<List<Book>>  getAllBooks();
    ResponseEntity<?> reserverBook(long bookId, String email);
    ResponseEntity<List<Book>> getBooksByPublished(boolean published);
    ResponseEntity<List<Book>> getBooksByTitle(String title);
    ResponseEntity<List<Book>> getBooksByTitleContains(String text);
    ResponseEntity<Book> getBookByIsbn(String isbn);
    ResponseEntity<List<Book>> getBooksByTitleOrDescription(String text);
    ResponseEntity<List<Book>> getBooksBetweenYears(int startYear, int endYear);
}
