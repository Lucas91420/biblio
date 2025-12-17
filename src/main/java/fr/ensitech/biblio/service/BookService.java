package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.Author;
import fr.ensitech.biblio.entity.Book;
import fr.ensitech.biblio.repository.IAuthorRepository;
import fr.ensitech.biblio.repository.IBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BookService implements IBookService {

    @Autowired
    private IBookRepository bookRepository;

    @Autowired
    private IAuthorRepository authorRepository;

    @Override
    public Book addOrUpdateBook(Book book) throws Exception {
        if (book.getId() != null && book.getId() < 0){
            throw new Exception("Book id must be greater than 0");
        }

        if(book.getId() == null || book.getId() == 0) {
            Book _book = bookRepository.findByIsbnIgnoreCase(book.getIsbn());
            if (_book != null) {
                throw new IllegalArgumentException("Book with same ISBN already exists");
            }
            book.getAuthors().forEach(a -> authorRepository.save(a));
            bookRepository.save(book);
        }

        if (book.getId() > 0) {
            Book _book = bookRepository.findById(book.getId()).orElse(null);
            if (_book == null) {
                throw new Exception("Book to update not found");
            }
            _book.setIsbn(book.getIsbn());
            _book.setTitle(book.getTitle());
            _book.setDescription(book.getDescription());
            _book.setEditor(book.getEditor());
            _book.setPublicationDate(book.getPublicationDate());
            _book.setCategory(book.getCategory());
            _book.setLanguage(book.getLanguage());
            _book.setNbPages(book.getNbPages());
            //_book.setPublished(book.getpublished());
            bookRepository.save(_book);
        }
        return book;
    }

    @Override
    public void deleteBook(long id) throws Exception {
        //bookRepository.deleteById(id);

        if (id <= 0) {
            throw new IllegalArgumentException("Book id must be > 0");
        }
        Book book = bookRepository.findById(id).orElse(null);
        if(book == null){
            throw new IllegalArgumentException("Book not found !");
        }
        bookRepository.deleteById(id);
    }

    /*@Override
    public List<Book> getBooks() throws Exception {
        return bookRepository.findAll();
    }

    @Override
    public Book getBook(long id) throws Exception {
        Optional<Book> optional = bookRepository.findById(id);
        return optional.orElse(null);
    }



    // 1
    @Override
    public List<Book> getBooksByPublished(boolean published) throws Exception {
        return bookRepository.findByPublished(published);
    }*/

    @Override
    public List<Book> getBooks() throws Exception {
        return bookRepository.findAll();
    }

    @Override
    public Book getBook(long id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("Book id must be > 0");
        }
        return bookRepository.findById(id).orElse(null);
    }

    // 1
    @Override
    public List<Book> getBooksByPublished(boolean published) throws Exception {
        return bookRepository.findByPublished(published);
    }

    // 2
    @Override
    public List<Book> getBooksByTitle(String title) throws Exception {
        return bookRepository.findByTitleIgnoreCase(title);
    }

    @Override
    public List<Book> getBooksByAuthor(Author author) throws Exception {
        return null;
    }
    @Override
    public List<Book> getBooksByTitleContains(String text) throws Exception {
        return bookRepository.findByTitleContainingIgnoreCase(text);
    }

    // 4
    @Override
    public Book getBookByIsbn(String isbn) throws Exception {
        return bookRepository.findByIsbnIgnoreCase(isbn);
    }

    // 5
    @Override
    public List<Book> getBooksByTitleOrDescription(String title, String description) throws Exception {
        return bookRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(title, description);
    }

    // 6
    @Override
    public List<Book> getBooksBetweenYears(int startYear, int endYear) throws Exception {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.YEAR, startYear);
        startCalendar.set(Calendar.MONTH, Calendar.JANUARY);
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = startCalendar.getTime();

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.YEAR, endYear);
        endCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        endCalendar.set(Calendar.DAY_OF_MONTH, 31);
        Date endDate = endCalendar.getTime();

        return bookRepository.findByPublicationDateBetween(startDate, endDate);
    }
}

