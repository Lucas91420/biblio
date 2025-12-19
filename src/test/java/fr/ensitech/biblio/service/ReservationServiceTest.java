package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.Book;
import fr.ensitech.biblio.entity.Reservation;
import fr.ensitech.biblio.entity.User;
import fr.ensitech.biblio.repository.IBookRepository;
import fr.ensitech.biblio.repository.IReservationRepository;
import fr.ensitech.biblio.repository.IUserRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private IReservationRepository reservationRepository;
    @Mock private IUserRepository userRepository;
    @Mock private IBookRepository bookRepository;

    @InjectMocks private ReservationService reservationService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("lucas@test.com");

        book = new Book();
        book.setId(10L);
        book.setTitle("Java");
        book.setStock(2);
    }

    @Test
    @DisplayName("Doit échouer si l'utilisateur n'existe pas")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("lucas@test.com")).thenReturn(null);

        assertThatThrownBy(() -> reservationService.reserveBook(10L, "lucas@test.com"))
                .isInstanceOf(Exception.class)
                .hasMessage("Utilisateur non trouvé");

        verifyNoInteractions(bookRepository);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    @DisplayName("Doit échouer si le livre n'existe pas")
    void shouldThrowWhenBookNotFound() {
        when(userRepository.findByEmail("lucas@test.com")).thenReturn(user);
        when(bookRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.reserveBook(10L, "lucas@test.com"))
                .isInstanceOf(Exception.class)
                .hasMessage("Livre non trouvé");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doit échouer si stock est null")
    void shouldThrowWhenStockNull() {
        book.setStock(null);

        when(userRepository.findByEmail("lucas@test.com")).thenReturn(user);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> reservationService.reserveBook(10L, "lucas@test.com"))
                .isInstanceOf(Exception.class)
                .hasMessage("Stock non défini ou nul");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doit échouer si stock <= 0")
    void shouldThrowWhenStockZero() {
        book.setStock(0);

        when(userRepository.findByEmail("lucas@test.com")).thenReturn(user);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> reservationService.reserveBook(10L, "lucas@test.com"))
                .isInstanceOf(Exception.class)
                .hasMessage("Stock non défini ou nul");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doit échouer si déjà une réservation active pour ce livre")
    void shouldThrowWhenAlreadyReservedSameBook() {
        when(userRepository.findByEmail("lucas@test.com")).thenReturn(user);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(reservationRepository.existsByUserAndBookAndActiveTrue(user, book)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.reserveBook(10L, "lucas@test.com"))
                .isInstanceOf(Exception.class)
                .hasMessage("Vous avez déjà une réservation active pour ce livre");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doit échouer si l'utilisateur a déjà 3 réservations actives")
    void shouldThrowWhenUserHasAlready3ActiveReservations() {
        when(userRepository.findByEmail("lucas@test.com")).thenReturn(user);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(reservationRepository.existsByUserAndBookAndActiveTrue(user, book)).thenReturn(false);

        List<Reservation> active = List.of(new Reservation(), new Reservation(), new Reservation());
        when(reservationRepository.findByUserAndActiveTrue(user)).thenReturn(active);

        assertThatThrownBy(() -> reservationService.reserveBook(10L, "lucas@test.com"))
                .isInstanceOf(Exception.class)
                .hasMessage("Vous avez déjà 3 livres réservés (limite atteinte)");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doit échouer si stock atteint (nb réservations actives >= stock)")
    void shouldThrowWhenNoStockAvailable() {
        when(userRepository.findByEmail("lucas@test.com")).thenReturn(user);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(reservationRepository.existsByUserAndBookAndActiveTrue(user, book)).thenReturn(false);
        when(reservationRepository.findByUserAndActiveTrue(user)).thenReturn(List.of()); // 0 réservations user

        // stock=2 et déjà 2 réservations actives
        when(reservationRepository.countByBookAndActiveTrue(book)).thenReturn(2L);

        assertThatThrownBy(() -> reservationService.reserveBook(10L, "lucas@test.com"))
                .isInstanceOf(Exception.class)
                .hasMessage("Plus de stock disponible pour ce livre");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Doit réussir et sauvegarder une réservation active")
    void shouldReserveBookSuccessfully() {
        when(userRepository.findByEmail("lucas@test.com")).thenReturn(user);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(reservationRepository.existsByUserAndBookAndActiveTrue(user, book)).thenReturn(false);
        when(reservationRepository.findByUserAndActiveTrue(user)).thenReturn(List.of()); // <3
        when(reservationRepository.countByBookAndActiveTrue(book)).thenReturn(1L); // < stock(2)

        reservationService.reserveBook(10L, "lucas@test.com");

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());

        Reservation saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getBook()).isEqualTo(book);
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getReservationDate()).isNotNull();
    }
}
