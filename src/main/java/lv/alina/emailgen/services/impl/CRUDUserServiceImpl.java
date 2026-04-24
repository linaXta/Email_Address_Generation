package lv.alina.emailgen.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.repos.ICompanyRepo;
import lv.alina.emailgen.repos.IDeletedGeneratedEmailRepo;
import lv.alina.emailgen.repos.IGeneratedEmailRepo;
import lv.alina.emailgen.repos.IMainEmailHistoryRepo;
import lv.alina.emailgen.repos.IMainEmailRepo;
import lv.alina.emailgen.repos.IShortCodesRepo;
import lv.alina.emailgen.repos.ISymbolRepo;
import lv.alina.emailgen.repos.IUserRepo;
import lv.alina.emailgen.service.ICRUDUserService;

@Service
public class CRUDUserServiceImpl implements ICRUDUserService{
	
	private final IUserRepo userRepo;
	private final IMainEmailRepo mainEmailRepo;
	private final PasswordEncoder passwordEncoder;
	private final IGeneratedEmailRepo generatedEmailRepo;
	private final IDeletedGeneratedEmailRepo deletedGeneratedEmailRepo;
	private final IMainEmailHistoryRepo mainEmailHistoryRepo;
	private final ICompanyRepo companyRepo;
	private final IShortCodesRepo shortCodesRepo;
	private final ISymbolRepo symbolRepo;
	
	
	public CRUDUserServiceImpl(IUserRepo userRepo, IMainEmailRepo mainEmailRepo, PasswordEncoder passwordEncoder, IGeneratedEmailRepo generatedEmailRepo, IDeletedGeneratedEmailRepo deletedGeneratedEmailRepo, IMainEmailHistoryRepo mainEmailHistoryRepo, ICompanyRepo companyRepo,IShortCodesRepo shortCodesRepo, ISymbolRepo symbolRepo ) {
		
        this.userRepo = userRepo;
        this.mainEmailRepo = mainEmailRepo;
        this.passwordEncoder = passwordEncoder;
        this.generatedEmailRepo = generatedEmailRepo;
        this.deletedGeneratedEmailRepo = deletedGeneratedEmailRepo;
        this.mainEmailHistoryRepo = mainEmailHistoryRepo;
        this.companyRepo = companyRepo;
        this.shortCodesRepo = shortCodesRepo;
        this.symbolRepo = symbolRepo;
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
    @Transactional
    public void deleteById(Long id) throws Exception {
    	User user = userRepo.findById(id).orElseThrow(() -> new Exception("User not found"));

        List<MainEmail> mainEmails = mainEmailRepo.findByUser(user);

        for (MainEmail mainEmail : mainEmails) {
            generatedEmailRepo.deleteByMainEmail(mainEmail);
            deletedGeneratedEmailRepo.deleteByMainEmail(mainEmail);
            mainEmailHistoryRepo.deleteByMainEmail(mainEmail);
        }

        mainEmailRepo.deleteByUser(user);
        companyRepo.deleteByUser(user);
        shortCodesRepo.deleteByUser(user);
        symbolRepo.deleteByUser(user);

        userRepo.delete(user);
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
    	
    	String originalEmail = email.trim();
    	String normalizedEmail = originalEmail.toLowerCase();
    	
    	if (!isEmailFormatValid(originalEmail)) {
            throw new Exception("Please enter a valid e-mail address.");
        }
    	
    	if (userRepo.existsByEmail(normalizedEmail)) {
            throw new Exception("User with this email already exists");
        }
    	
    	String passwordHash = passwordEncoder.encode(rawPassword);
    	
    	User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordHash);
        user.setFullName(null);
        user.setMfaEnabled(false);
        
        User savedUser = userRepo.save(user);
        
        MainEmail firstMainEmail = new MainEmail();
        firstMainEmail.setUser(savedUser);
        firstMainEmail.setMainEmail(originalEmail);

        mainEmailRepo.save(firstMainEmail);

        return savedUser;
    	
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
    
    @Override
    public boolean existsByEmail(String email) throws Exception {
        if (email == null || email.isBlank()) {
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase();
        return userRepo.existsByEmail(normalizedEmail);
    }
    
    @Override
    public User markUserLoggedIn(String email) throws Exception {
        if (email == null || email.isBlank()) {
            throw new Exception("E-mail is required.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepo.findByEmail(normalizedEmail);
        if (user == null) {
            throw new Exception("User not found.");
        }

        user.setLastLoginAt(LocalDateTime.now());

        return userRepo.save(user);
    }
    
    @Override
    public User loginUser(String email, String rawPassword) throws Exception {
        if (email == null || email.isBlank()) {
            throw new Exception("E-mail is needed");
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new Exception("Password is needed");
        }

        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepo.findByEmail(normalizedEmail);

        if (user == null) {
            throw new Exception("Incorrect e-mail or password");
        }

        boolean passwordMatches = passwordEncoder.matches(rawPassword, user.getPasswordHash());

        if (!passwordMatches) {
            throw new Exception("Incorrect e-mail or password");
        }

        user.setLastLoginAt(java.time.LocalDateTime.now());

        return userRepo.save(user);
    }
    
    @Override
    public void updatePasswordByEmail(String email, String newRawPassword) throws Exception {
        if (email == null || email.isBlank()) {
            throw new Exception("E-mail is required");
        }

        if (newRawPassword == null || newRawPassword.isBlank()) {
            throw new Exception("Password is required");
        }

        String normalizedEmail = email.trim().toLowerCase() ;

        User user = userRepo.findByEmail(normalizedEmail);

        if (user == null) {
            throw new Exception("User with this e-mail was not found");
        }

        String passwordHash = passwordEncoder.encode(newRawPassword);
        user.setPasswordHash(passwordHash);

        userRepo.save(user);
    }
    
    private boolean isEmailFormatValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String value = email.trim();

        return value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

}
