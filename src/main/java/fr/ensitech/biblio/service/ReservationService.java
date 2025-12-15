package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.Book;
import fr.ensitech.biblio.entity.Reservation;
import fr.ensitech.biblio.entity.User;
import fr.ensitech.biblio.repository.IBookRepository;
import fr.ensitech.biblio.repository.IReservationRepository;
import fr.ensitech.biblio.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReservationService implements IReservationService {

    @Autowired
    private IReservationRepository reservationRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IBookRepository bookRepository;

    @Override
    public void reserveBook(Long bookId, String userEmail) throws Exception {

        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new Exception("Utilisateur non trouvé");
        }
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            throw new Exception("Livre non trouvé");
        }
        if (book.getStock() == null || book.getStock() <= 0) {
            throw new Exception("Stock non défini ou nul");
        }
        boolean alreadyReserved = reservationRepository.existsByUserAndBookAndActiveTrue(user, book);
        if (alreadyReserved) {
            throw new Exception("Vous avez déjà une réservation active pour ce livre");
        }
        List<Reservation> userActiveReservations = reservationRepository.findByUserAndActiveTrue(user);
        if (userActiveReservations.size() >= 3) {
            throw new Exception("Vous avez déjà 3 livres réservés (limite atteinte)");
        }
        long nbReservationsForBook = reservationRepository.countByBookAndActiveTrue(book);
        if (nbReservationsForBook >= book.getStock()) {
            throw new Exception("Plus de stock disponible pour ce livre");
        }
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReservationDate(new Date());
        reservation.setActive(true);
        reservationRepository.save(reservation);
    }
}
