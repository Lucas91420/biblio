package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.Author;

import java.util.List;

public interface IAuthorService {


    void createAuthor(Author author) throws Exception;
    List<Author> getAuthors(String firstname) throws Exception;
    List<Author> getAuthors(String firstname, String lastName) throws Exception;
}
