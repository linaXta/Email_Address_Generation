package lv.alina.emailgen.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lv.alina.emailgen.models.Company;
import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.models.enums.VerificationCodeStatus;
import lv.alina.emailgen.repos.ICompanyRepo;
import lv.alina.emailgen.repos.IDeletedGeneratedEmailRepo;
import lv.alina.emailgen.repos.IGeneratedEmailRepo;
import lv.alina.emailgen.repos.IMainEmailHistoryRepo;
import lv.alina.emailgen.repos.IMainEmailRepo;
import lv.alina.emailgen.repos.IShortCodeCounterRepo;
import lv.alina.emailgen.repos.IShortCodesRepo;
import lv.alina.emailgen.repos.ISymbolRepo;
import lv.alina.emailgen.repos.IUserRepo;
import lv.alina.emailgen.service.IAccountDeletionVerificationService;
import lv.alina.emailgen.service.ICRUDUserService;
import lv.alina.emailgen.service.IEmailService;
import lv.alina.emailgen.service.IVerificationCodeService;

@Service
public class CRUDUserServiceImpl implements ICRUDUserService{
	
	private final PasswordEncoder passwordEncoder;
	
	private final IUserRepo userRepo;
	private final IMainEmailRepo mainEmailRepo;
	private final IGeneratedEmailRepo generatedEmailRepo;
	private final IDeletedGeneratedEmailRepo deletedGeneratedEmailRepo;
	private final IMainEmailHistoryRepo mainEmailHistoryRepo;
	private final ICompanyRepo companyRepo;
	private final IShortCodesRepo shortCodesRepo;
	private final ISymbolRepo symbolRepo;
	private final IShortCodeCounterRepo shortCodeCounterRepo;

	private final IVerificationCodeService verificationService;
	private final IEmailService emailService;
	private final IAccountDeletionVerificationService accountDeletionVerificationService;
	
	
	public CRUDUserServiceImpl(
	        IUserRepo userRepo,
	        IMainEmailRepo mainEmailRepo,
	        PasswordEncoder passwordEncoder,
	        IGeneratedEmailRepo generatedEmailRepo,
	        IDeletedGeneratedEmailRepo deletedGeneratedEmailRepo,
	        IMainEmailHistoryRepo mainEmailHistoryRepo,
	        ICompanyRepo companyRepo,
	        IShortCodesRepo shortCodesRepo,
	        ISymbolRepo symbolRepo,
	        IShortCodeCounterRepo shortCodeCounterRepo,
	        IVerificationCodeService verificationService,
	        IEmailService emailService,
	        IAccountDeletionVerificationService accountDeletionVerificationService
	) {
		
		this.userRepo = userRepo;
        this.mainEmailRepo = mainEmailRepo;
        this.passwordEncoder = passwordEncoder;
        this.generatedEmailRepo = generatedEmailRepo;
        this.deletedGeneratedEmailRepo = deletedGeneratedEmailRepo;
        this.mainEmailHistoryRepo = mainEmailHistoryRepo;
        this.companyRepo = companyRepo;
        this.shortCodesRepo = shortCodesRepo;
        this.symbolRepo = symbolRepo;
        this.shortCodeCounterRepo = shortCodeCounterRepo;
        this.verificationService = verificationService;
        this.emailService = emailService;
        this.accountDeletionVerificationService = accountDeletionVerificationService;
        
    }

    @Override
    public ArrayList<User> retrieveAll() throws Exception {
        ArrayList<User> users = new ArrayList<>();
        userRepo.findAll().forEach(users::add);
        return users;
    }

    @Override
    public User retrieveById(Long id) throws Exception {
        return userRepo.findById(id).orElseThrow(() -> new Exception("User is not found with id = " + id));
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
        
        List<Company> companies = companyRepo.findByUser(user);

        for (Company company : companies) {
            generatedEmailRepo.deleteByCompany(company);
            shortCodeCounterRepo.deleteByCompany(company);
        }
        
        mainEmailRepo.deleteByUser(user);
        shortCodesRepo.deleteByUser(user);
        symbolRepo.deleteByUser(user);
        companyRepo.deleteByUser(user);

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
    
    @Override
    public boolean isPasswordCorrect(User user, String rawPassword) throws Exception {
        if (user == null) {
            throw new Exception("User is required");
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            return false;
        }

        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
    
    private boolean isEmailFormatValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String value = email.trim();

        return value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    @Override
    public User updateFullName(User user, String fullName) throws Exception {
        if (user == null) {
            throw new Exception("User is required");
        }

        User existingUser = userRepo.findById(user.getUserId()).orElseThrow(() -> new Exception("User not found"));

        if (fullName == null || fullName.isBlank()) {
            existingUser.setFullName(null);
        } else {
            existingUser.setFullName(fullName.trim());
        }

        return userRepo.save(existingUser);
    }

    @Override
    public void changePassword(User user, String currentPassword, String newPassword, String repeatedPassword) throws Exception {
        if (user == null) {
            throw new Exception("User is required");
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new Exception("Current password is required");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new Exception("New password is required");
        }

        if (repeatedPassword == null || repeatedPassword.isBlank()) {
            throw new Exception("Repeat password is required");
        }

        if (!newPassword.equals(repeatedPassword)) {
            throw new Exception("New passwords do not match");
        }

        if (newPassword.length() < 8) {
            throw new Exception("New password must be at least 8 characters long");
        }

        User existingUser = userRepo.findById(user.getUserId()).orElseThrow(() -> new Exception("User not found"));

        if (!passwordEncoder.matches(currentPassword, existingUser.getPasswordHash())) {
            throw new Exception("Current password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, existingUser.getPasswordHash())) {
            throw new Exception("New password cannot be the same as current password");
        }

        String passwordHash = passwordEncoder.encode(newPassword);
        existingUser.setPasswordHash(passwordHash);

        userRepo.save(existingUser);
    }
    
    @Override
    public void requestEmailChange(User user, String currentPassword, String newEmail) throws Exception {
        if (user == null) {
            throw new Exception("User is required");
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new Exception("Current password is required");
        }

        if (newEmail == null || newEmail.isBlank()) {
            throw new Exception("New e-mail is required");
        }

        String normalizedNewEmail = newEmail.trim().toLowerCase();

        if (!isEmailFormatValid(normalizedNewEmail)) {
            throw new Exception("Please enter a valid e-mail address");
        }

        User existingUser = userRepo.findById(user.getUserId()).orElseThrow(() -> new Exception("User not found"));

        if (!passwordEncoder.matches(currentPassword, existingUser.getPasswordHash())) {
            throw new Exception("Current password is incorrect");
        }

        if (existingUser.getEmail().equalsIgnoreCase(normalizedNewEmail)) {
            throw new Exception("New e-mail cannot be the same as current e-mail");
        }

        if (userRepo.existsByEmail(normalizedNewEmail)) {
            throw new Exception("User with this e-mail already exists");
        }

        if (!verificationService.canResendCode(normalizedNewEmail)) {
            int seconds = verificationService.getRemainingResendCooldownSeconds(normalizedNewEmail);
            throw new Exception("Please wait " + seconds + " seconds before requesting a new code");
        }

        String code = verificationService.createAndStoreCode(normalizedNewEmail);

        emailService.sendVerificationCode(normalizedNewEmail, "Confirm e-mail change",
                "Your e-mail change confirmation code is: " + code
                        + "\n\nThis code is valid for 10 minutes."
                        + "\n\nIf you did not request this change, please ignore this email."
        );
    }

    @Override
    public User confirmEmailChange(User user, String newEmail, String code) throws Exception {
        if (user == null) {
            throw new Exception("User is required");
        }

        if (newEmail == null || newEmail.isBlank()) {
            throw new Exception("New e-mail is required");
        }

        if (code == null || code.isBlank()) {
            throw new Exception("Verification code is required");
        }

        String normalizedNewEmail = newEmail.trim().toLowerCase();
        
        if (!isEmailFormatValid(normalizedNewEmail)) {
            throw new Exception("Please enter a valid e-mail address");
        }

        VerificationCodeStatus status = verificationService.getCodeStatus(normalizedNewEmail, code.trim());

        if (status != VerificationCodeStatus.VALID) {
            throw new Exception("Verification code is invalid or expired");
        }

        if (userRepo.existsByEmail(normalizedNewEmail)) {
            throw new Exception("User with this e-mail already exists");
        }

        User existingUser = userRepo.findById(user.getUserId()).orElseThrow(() -> new Exception("User not found"));

        existingUser.setEmail(normalizedNewEmail);

        User savedUser = userRepo.save(existingUser);

        verificationService.removeCode(normalizedNewEmail);

        return savedUser;
    }
    
    @Override
    public void requestAccountDeletion(User user, String currentPassword) throws Exception {
        if (user == null) {
            throw new Exception("User is required");
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new Exception("Current password is required");
        }

        User existingUser = userRepo.findById(user.getUserId()).orElseThrow(() -> new Exception("User not found"));

        if (!passwordEncoder.matches(currentPassword, existingUser.getPasswordHash())) {
            throw new Exception("Current password is incorrect");
        }

        accountDeletionVerificationService.sendDeleteConfirmationCode(existingUser);
    }
    
    @Override
    @Transactional
    public void confirmAccountDeletion(User user, String code) throws Exception {
        if (user == null) {
            throw new Exception("User is required");
        }

        if (code == null || code.isBlank()) {
            throw new Exception("Verification code is required");
        }

        User existingUser = userRepo.findById(user.getUserId()).orElseThrow(() -> new Exception("User not found"));

        boolean isCodeValid = accountDeletionVerificationService.verifyDeleteConfirmationCode(existingUser.getEmail(), code);

        if (!isCodeValid) {
            throw new Exception("Verification code is invalid or expired");
        }

        deleteById(existingUser.getUserId());
    }

}
