package fr.ensitech.biblio.service;

public interface IReservationService {

    void reserveBook(Long bookId, String userEmail) throws Exception;
}
