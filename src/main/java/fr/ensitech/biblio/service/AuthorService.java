package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.Author;
import fr.ensitech.biblio.repository.IAuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService implements IAuthorService {

    @Autowired
    private IAuthorRepository authorRepository;


    @Override
    public List<Author> getAuthors(String firstname, String lastname) throws Exception {
        return authorRepository.findAuthors(firstname, lastname);
    }

    @Override
    public List<Author> getAuthors(String firstname) throws Exception {
        return authorRepository.findByFirstnameIgnoreCase(firstname);
    }

    public void createAuthor(Author author)throws Exception{
        authorRepository.save(author);
    }
}
