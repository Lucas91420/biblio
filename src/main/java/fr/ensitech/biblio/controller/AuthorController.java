package fr.ensitech.biblio.controller;

import fr.ensitech.biblio.entity.Author;
import fr.ensitech.biblio.service.IAuthorService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/authors")
public class AuthorController implements IAuthorController{

    @Autowired
    private IAuthorService authorService;

    // => http://localhost:8080/api/authors/create
    @Override
    @PostMapping("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<Author> createAuthor(@RequestBody Author author) {
        System.out.println("createAuthor invoked");
        if (author == null
                || author.getFirstname() == null || author.getFirstname().isBlank()
                || author.getLastname() == null || author.getLastname().isBlank()) {

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            authorService.createAuthor(author);
            return new ResponseEntity<Author>(author, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
