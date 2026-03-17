package lv.alina.emailgen.services.impl;

import java.util.ArrayList;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lv.alina.emailgen.models.User;
import lv.alina.emailgen.repos.IUserRepo;
import lv.alina.emailgen.service.ICRUDUserService;

@Service
public class CRUDUserServiceImpl implements ICRUDUserService{
	
	private final IUserRepo userRepo;
	private final PasswordEncoder passwordEncoder;
	
	public CRUDUserServiceImpl(IUserRepo userRepo,PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ArrayList<User> retrieveAll() throws Exception {
        ArrayList<User> users = new ArrayList<>();
        userRepo.findAll().forEach(users::add);
        return users;
    }


    @Override
    public User retrieveById(Long id) throws Exception {
        return userRepo.findById(id)
            .orElseThrow(() -> new Exception("User is not found with id = " + id));
    }

    
    @Override
    public void deleteById(Long id) throws Exception {
        if (!userRepo.existsById(id)) {
            throw new Exception("User is not found with id = " + id);
        }
        userRepo.deleteById(id);
    }

    @Override
    public User createUser(String email, String passwordHash, String fullName) throws Exception {
        if (userRepo.existsByEmail(email)) {
            throw new Exception("User with this email already exists: " + email);
        }

        User user = new User(email, passwordHash, fullName);
        return userRepo.save(user);
    }
    
    @Override
    public User registerUser(String email, String rawPassword) throws Exception {
    	if (email == null || email.isBlank()) {
    		throw new Exception("Email is needed");
    	}
    	
    	if (rawPassword == null || rawPassword.isBlank()) {
    		throw new Exception("Password is needed");
    	}
    	
    	String normalizedEmail = email.trim().toLowerCase();
    	
    	if (userRepo.existsByEmail(normalizedEmail)) {
            throw new Exception("User with this email already exists");
        }
    	
    	String passwordHash = passwordEncoder.encode(rawPassword);
    	
    	User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordHash);
        user.setFullName(null);
        user.setMfaEnabled(false);
        
        return userRepo.save(user);
    	
    }

    @Override
    public User updateUser(Long id, String email, String fullName, boolean mfaEnabled) throws Exception {
        User user = userRepo.findById(id)
            .orElseThrow(() -> new Exception("User is not found with id = " + id));

        user.setEmail(email);
        user.setFullName(fullName);
        user.setMfaEnabled(mfaEnabled);

        return userRepo.save(user);
    }

}
