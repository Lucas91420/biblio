package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.Author;
import fr.ensitech.biblio.entity.Book;

import java.util.Date;
import java.util.List;

public interface IBookService {

    void addOrUpdateBook(Book book) throws Exception;
    void deleteBook(long id) throws Exception;
    List<Book> getBooks() throws Exception;
    Book getBook(long id) throws Exception;
    List<Book> getBooksByTitle(String title) throws Exception;
    List<Book> getBooksByAuthor(Author author) throws Exception;
    List<Book> getBooksBetweenYears(int startYear, int endYear) throws Exception;
    List<Book> getBooksByPublished(boolean published) throws Exception;
    Book getBookByIsbn(String isbn) throws Exception;
    List<Book> getBooksByTitleOrDescription(String title, String description) throws Exception;
    List<Book> getBooksByTitleContains(String text) throws Exception;
}
