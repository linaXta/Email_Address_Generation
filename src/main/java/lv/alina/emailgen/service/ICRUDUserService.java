package lv.alina.emailgen.service;

import java.util.ArrayList;

import lv.alina.emailgen.models.User;
import lv.alina.emailgen.repos.IMainEmailRepo;

public interface ICRUDUserService {
	
	ArrayList<User> retrieveAll() throws Exception;
	User retrieveById(Long id) throws Exception;
	void deleteById(Long id) throws Exception;
    
	User createUser(String email, String passwordHash, String fullName) throws Exception;
	
    User updateUser(Long id, String email, String fullName, boolean mfaEnabled) throws Exception;
    
    User registerUser(String email, String rawPassword) throws Exception;
    
    boolean existsByEmail(String email) throws Exception;
    
    User markUserLoggedIn(String email) throws Exception;
    
    User loginUser(String email, String rawPassword) throws Exception;
    
    void updatePasswordByEmail(String email, String newRawPassword) throws Exception;
    
    boolean isPasswordCorrect(User user, String rawPassword) throws Exception;
    
    User updateFullName(User user, String fullName) throws Exception;

    void changePassword(User user, String currentPassword, String newPassword, String repeatedPassword) throws Exception;
    
    void requestEmailChange(User user, String currentPassword, String newEmail) throws Exception;

    User confirmEmailChange(User user, String newEmail, String code) throws Exception;

    void requestAccountDeletion(User user, String currentPassword) throws Exception;

    void confirmAccountDeletion(User user, String code) throws Exception;
}
