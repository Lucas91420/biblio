package fr.ensitech.biblio.controller;

import fr.ensitech.biblio.entity.Book;
import fr.ensitech.biblio.service.IBookService;
import fr.ensitech.biblio.service.IReservationService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.print.attribute.standard.Media;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/books")
public class BookController implements IBookController{

    @Autowired
    private IBookService bookService;

    @Autowired
    private IReservationService reservationService;

    @PostMapping("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        if (book == null
                || book.getIsbn() == null || book.getIsbn().isBlank()
                || book.getTitle() == null || book.getTitle().isBlank()
                || book.getDescription() == null || book.getDescription().isBlank()
                || book.getEditor() == null || book.getEditor().isBlank()
                || book.getStock() == null || book.getStock() <= 0
                || book.getPublicationDate() == null
                || book.getCategory() == null || book.getCategory().isBlank()
                || book.getLanguage() == null || book.getLanguage().isBlank()
                || book.getNbPages() <= 0){

            return new ResponseEntity<Book>(book, HttpStatus.BAD_REQUEST);
        }
        try {
            bookService.addOrUpdateBook(book);
            return new ResponseEntity<Book>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<Book>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    @GetMapping("/{id}")
    @Override
    public ResponseEntity<Book> getBookById(@PathVariable("id") @RequestParam(required = true) long id) {
       if (id < 0){
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
       }
       try {
           Book book = bookService.getBook(id);
           return new ResponseEntity<>(book, HttpStatus.OK);
       } catch (Exception e){
           return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @PutMapping("/update")
    @Override
    public ResponseEntity<Book> updateBook(@RequestBody Book book) {
        if (book == null
                || book.getId() <= 0
                || book.getIsbn() == null || book.getIsbn().isBlank()
                || book.getStock() == null || book.getStock() <= 0
                || book.getTitle() == null || book.getTitle().isBlank()
                || book.getDescription() == null || book.getDescription().isBlank()
                || book.getEditor() == null || book.getEditor().isBlank()
                || book.getPublicationDate() == null
                || book.getCategory() == null || book.getCategory().isBlank()
                || book.getLanguage() == null || book.getLanguage().isBlank()
                || book.getNbPages() <= 0) {

            return new ResponseEntity<Book>(HttpStatus.BAD_REQUEST);
        }
        try {
            bookService.addOrUpdateBook(book);
            Book _book = bookService.getBook(book.getId());
            return new ResponseEntity<Book>(_book, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<Book>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/remove/{id}")
    @Override
    public ResponseEntity<String> deleteBook(@PathVariable("id") @RequestParam(required = true) long id) {
        if (id <= 0)
         {
             return new ResponseEntity<String>("book id must be greater than 0", HttpStatus.BAD_REQUEST);

         }
        try {
            bookService.deleteBook(id);
            String message = "Book delete with success [id = ".concat(String.valueOf(id)).concat("]");
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e){
            String error = "Erreur interne : " + e.getMessage();
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/all")
    @Override
    public ResponseEntity<List<Book>> getAllBooks() {
        try{
            List<Book> books = bookService.getBooks();
            if(books == null || books.isEmpty()){
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(books, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/reserver/{book_id}/{email}")
    @Override
    public ResponseEntity<?> reserverBook(@PathVariable("book_id") long bookId,
                                          @PathVariable("email") String email) {

        Map<String, String> body = new HashMap<>();

        if (bookId <= 0 || email == null || email.isBlank()) {
            body.put("message", "Paramètres invalides");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        try {
            reservationService.reserveBook(bookId, email);
            body.put("message", "Réservation effectuée avec succès");
            return new ResponseEntity<>(body, HttpStatus.OK);
        } catch (Exception e) {
            body.put("message", "Erreur: " + e.getMessage());
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/published/{published}")
    @Override
    public ResponseEntity<List<Book>> getBooksByPublished(
            @PathVariable boolean published) {

        try {
            List<Book> books = bookService.getBooksByPublished(published);
            if (books.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/title/{title}")
    @Override
    public ResponseEntity<List<Book>> getBooksByTitle(
            @PathVariable String title) {

        if (title == null || title.isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            List<Book> books = bookService.getBooksByTitle(title);
            if (books.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/title/contains/{text}")
    @Override
    public ResponseEntity<List<Book>> getBooksByTitleContains(
            @PathVariable String text) {

        if (text == null || text.isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            List<Book> books = bookService.getBooksByTitleContains(text);
            if (books.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/isbn/{isbn}")
    @Override
    public ResponseEntity<Book> getBookByIsbn(
            @PathVariable String isbn) {

        if (isbn == null || isbn.isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Book book = bookService.getBookByIsbn(isbn);
            if (book == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/search/{text}")
    @Override
    public ResponseEntity<List<Book>> getBooksByTitleOrDescription(
            @PathVariable String text) {

        if (text == null || text.isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            List<Book> books = bookService.getBooksByTitleOrDescription(text, text);
            if (books.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/between/{startYear}/{endYear}")
    @Override
    public ResponseEntity<List<Book>> getBooksBetweenYears(
            @PathVariable int startYear,
            @PathVariable int endYear) {

        if (startYear <= 0 || endYear <= 0 || startYear > endYear) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            List<Book> books = bookService.getBooksBetweenYears(startYear, endYear);
            if (books.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
