package lv.alina.emailgen.service;

import lv.alina.emailgen.models.User;

public interface ICRUDUserService extends ICRUDBase<User>{
    
	User createUser(String email, String passwordHash, String fullName) throws Exception;
	
    User updateUser(Long id, String email, String fullName, boolean mfaEnabled) throws Exception;
    
    User registerUser(String email, String rawPassword) throws Exception;
    
    boolean existsByEmail(String email) throws Exception;

}
