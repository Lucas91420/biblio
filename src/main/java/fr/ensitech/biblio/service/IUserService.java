package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.User;

import java.util.Date;
import java.util.List;


public interface IUserService {

    void createUser(User user) throws Exception;
    User getUserById(long id)throws Exception;
    List<User> getUserByBirthdate(Date dateInf, Date dateSup) throws Exception;
    void register(User user) throws Exception;
    void activateAccount(String email) throws Exception;
    User login(String email, String password) throws Exception;
    void unsubscribe(String email) throws Exception;
    void updatePassword(long id, String oldPassword, String newPassword) throws Exception;
    void updateProfile(long id, User updatedUser) throws Exception;
    void verifySecurityAnswer(String email, String answer) throws Exception;
    void renewPassword(String email, String oldPassword, String newPassword) throws Exception;

}
