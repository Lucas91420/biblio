package fr.ensitech.biblio.controller;

import fr.ensitech.biblio.entity.Author;
import org.springframework.http.ResponseEntity;

public interface IAuthorController {

    ResponseEntity<Author> createAuthor(Author author);
}
