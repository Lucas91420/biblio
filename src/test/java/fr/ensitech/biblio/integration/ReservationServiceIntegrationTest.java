package fr.ensitech.biblio.integration;

import fr.ensitech.biblio.entity.Book;
import fr.ensitech.biblio.entity.Reservation;
import fr.ensitech.biblio.entity.User;
import fr.ensitech.biblio.repository.IBookRepository;
import fr.ensitech.biblio.repository.IReservationRepository;
import fr.ensitech.biblio.repository.IUserRepository;
import fr.ensitech.biblio.service.IReservationService;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationServiceIntegrationTest {

    @Autowired private IReservationService reservationService;
    @Autowired private IUserRepository userRepository;
    @Autowired private IBookRepository bookRepository;
    @Autowired private IReservationRepository reservationRepository;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        // --- User minimal (champs obligatoires)
        user = new User();
        user.setFirstname("Lucas");
        user.setLastname("Test");
        user.setEmail("lucas@test.com");
        user.setPassword("hash-bcrypt-dummy"); // ici on s'en fiche, réservation ne check pas le pwd
        user.setRole("U");
        user.setActive(true);
        user = userRepository.save(user);

        // --- Book minimal (champs obligatoires)
        book = new Book();
        book.setTitle("Java Book");
        book.setPublished(true);
        book.setIsbn("ISBN-RES-0001"); // unique
        book.setStock(2);
        book = bookRepository.save(book);
    }

    @Test
    @SneakyThrows
    @DisplayName("Réservation OK : crée une réservation active en base")
    void shouldReserveBookSuccessfully() {
        reservationService.reserveBook(book.getId(), user.getEmail());

        List<Reservation> reservations = reservationRepository.findByUserAndActiveTrue(user);
        assertThat(reservations).hasSize(1);

        Reservation r = reservations.get(0);
        assertThat(r.isActive()).isTrue();
        assertThat(r.getReservationDate()).isNotNull();
        assertThat(r.getBook().getId()).isEqualTo(book.getId());
    }

    @Test
    @DisplayName("Erreur : utilisateur introuvable")
    void shouldFailWhenUserNotFound() {
        assertThatThrownBy(() -> reservationService.reserveBook(book.getId(), "unknown@test.com"))
                .isInstanceOf(Exception.class)
                .hasMessage("Utilisateur non trouvé");
    }

    @Test
    @DisplayName("Erreur : livre introuvable")
    void shouldFailWhenBookNotFound() {
        assertThatThrownBy(() -> reservationService.reserveBook(999999L, user.getEmail()))
                .isInstanceOf(Exception.class)
                .hasMessage("Livre non trouvé");
    }

    @Test
    @DisplayName("Erreur : stock nul/non défini")
    void shouldFailWhenStockInvalid() {
        book.setStock(0);
        bookRepository.save(book);

        assertThatThrownBy(() -> reservationService.reserveBook(book.getId(), user.getEmail()))
                .isInstanceOf(Exception.class)
                .hasMessage("Stock non défini ou nul");
    }

    @Test
    @SneakyThrows
    @DisplayName("Erreur : déjà une réservation active pour le même livre")
    void shouldFailWhenAlreadyReservedSameBook() {
        // Réservation 1
        reservationService.reserveBook(book.getId(), user.getEmail());

        // Réservation 2 => doit échouer
        assertThatThrownBy(() -> reservationService.reserveBook(book.getId(), user.getEmail()))
                .isInstanceOf(Exception.class)
                .hasMessage("Vous avez déjà une réservation active pour ce livre");
    }

    @Test
    @SneakyThrows
    @DisplayName("Erreur : limite 3 réservations actives par user")
    void shouldFailWhenUserHas3ActiveReservations() {
        // créer 3 livres différents et réserver
        Book b2 = new Book();
        b2.setTitle("Book 2");
        b2.setPublished(true);
        b2.setIsbn("ISBN-RES-0002");
        b2.setStock(5);
        b2 = bookRepository.save(b2);

        Book b3 = new Book();
        b3.setTitle("Book 3");
        b3.setPublished(true);
        b3.setIsbn("ISBN-RES-0003");
        b3.setStock(5);
        b3 = bookRepository.save(b3);

        reservationService.reserveBook(book.getId(), user.getEmail());
        reservationService.reserveBook(b2.getId(), user.getEmail());
        reservationService.reserveBook(b3.getId(), user.getEmail());

        // 4ème tentative (sur un autre livre)
        Book b4 = new Book();
        b4.setTitle("Book 4");
        b4.setPublished(true);
        b4.setIsbn("ISBN-RES-0004");
        b4.setStock(5);
        b4 = bookRepository.save(b4);

        Book finalB = b4;
        assertThatThrownBy(() -> reservationService.reserveBook(finalB.getId(), user.getEmail()))
                .isInstanceOf(Exception.class)
                .hasMessage("Vous avez déjà 3 livres réservés (limite atteinte)");
    }

    @Test
    @SneakyThrows
    @DisplayName("Erreur : plus de stock (nb réservations actives >= stock)")
    void shouldFailWhenNoStockAvailable() {
        // book stock = 1
        book.setStock(1);
        bookRepository.save(book);

        // créer un autre user qui réserve avant
        User other = new User();
        other.setFirstname("Other");
        other.setLastname("Guy");
        other.setEmail("other@test.com");
        other.setPassword("hash-bcrypt-dummy");
        other.setRole("U");
        other.setActive(true);
        other = userRepository.save(other);

        reservationService.reserveBook(book.getId(), other.getEmail());

        // maintenant lucas tente => stock atteint
        assertThatThrownBy(() -> reservationService.reserveBook(book.getId(), user.getEmail()))
                .isInstanceOf(Exception.class)
                .hasMessage("Plus de stock disponible pour ce livre");
    }
}
