package fr.ensitech.biblio.repository;

import fr.ensitech.biblio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Date;
import java.util.List;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

    List<User> findByBirthdateBetween(Date startDate, Date enDate);
    User findByFirstname(String firstName);
    List<User> findByFirstnameAndLastname(String firstName, String lastName);
    List<User> findByBirthdate(Date birthdate);
    User findByEmail(String email);

}
