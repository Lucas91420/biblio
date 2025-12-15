package fr.ensitech.biblio.repository;

import fr.ensitech.biblio.entity.Book;
import fr.ensitech.biblio.entity.Reservation;
import fr.ensitech.biblio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserAndActiveTrue(User user);

    List<Reservation> findByBookAndActiveTrue(Book book);

    boolean existsByUserAndBookAndActiveTrue(User user, Book book);

    long countByBookAndActiveTrue(Book book);
}
