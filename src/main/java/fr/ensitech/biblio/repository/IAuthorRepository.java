package fr.ensitech.biblio.repository;

import fr.ensitech.biblio.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAuthorRepository extends JpaRepository<Author, Long> {

    List<Author> findByFirstnameIgnoreCase(String firstname);

    @Query ("select a from Author a where a.firstname = ?1 and a.lastname = ?2")
    List<Author> findAuthors(String firstname, String lastName);
}
