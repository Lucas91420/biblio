package fr.ensitech.biblio.controller;

import fr.ensitech.biblio.entity.User;
import org.springframework.http.ResponseEntity;

public interface IUserController {

    ResponseEntity<String> register(User user);

    ResponseEntity<String> activate(String email);

    ResponseEntity<String> login(String email, String password);

    ResponseEntity<String> unsubscribe(String email);

    ResponseEntity<String> updatePassword(long id, String oldPassword, String newPassword);

    ResponseEntity<String> updateProfile(long id, User user);

    ResponseEntity<String> verifySecurity(String email, String answer);

    ResponseEntity<String> renewPassword(String email, String oldPassword, String newPassword);

}
