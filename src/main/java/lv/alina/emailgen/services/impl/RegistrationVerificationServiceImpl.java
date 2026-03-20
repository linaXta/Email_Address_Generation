package lv.alina.emailgen.services.impl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lv.alina.emailgen.models.VerificationCodeData;
import lv.alina.emailgen.service.IRegistrationVerificationService;

@Service
public class RegistrationVerificationServiceImpl implements IRegistrationVerificationService{
	
	private static final int CODE_LENGHT = 6;
	private static final int EXPIRATION_IN_MINUTES = 10;
	
	private final Map<String, VerificationCodeData> verificationCodes = new ConcurrentHashMap<>();
	private final Random random = new Random();
	
	@Override
	public String createAndStoreCode(String email) throws Exception {
		String normalaizedEmail = normalaize(email);
		
		if (normalaizedEmail.isBlank()) {
			throw new Exception ("E-mail is required.");
		}
		
		String code = generateVerificationCode();
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_IN_MINUTES);
		
		verificationCodes.put(normalaizedEmail, new VerificationCodeData(code, expiresAt));
		
		return code;
	}
	

	@Override
    public boolean isCodeValid(String email, String code) {
    	String normalaizedEmail = normalaize(email);
    	String normalizedCode = code == null ? "" : code.trim();
    	
    	if (normalaizedEmail.isBlank() || normalizedCode.isBlank()) {
    		return false;
    	}
    	
    	VerificationCodeData storedDate = verificationCodes.get(normalaizedEmail);
    	
    	if (storedDate == null) {
    		return false;
    	}
    	
    	if (storedDate.getExpiresAt().isBefore(LocalDateTime.now())) {
    		verificationCodes.remove(normalaizedEmail);
    		return false;
    	}
    	
    	return storedDate.getCode().equals(normalizedCode);
    }

	@Override
    public void removeCode(String email) throws Exception{
		String normalaizedEmail = normalaize(email);
		
		if (!normalaizedEmail.isBlank()){
			verificationCodes.remove(normalaizedEmail);
		}
    }
	
	@Override
	public boolean hasActiveCode(String email) throws Exception {
	    String normalizedEmail = normalaize(email);

	    if (normalizedEmail.isBlank()) {
	        return false;
	    }

	    VerificationCodeData data = verificationCodes.get(normalizedEmail); // "test@mail.com" → { code: "123456", expiresAt: 12:30 }

	    if (data == null) {
	        return false;
	    }

	    if (data.getExpiresAt().isBefore(LocalDateTime.now())) {
	        verificationCodes.remove(normalizedEmail);
	        return false;
	    }

	    return true;
	}
	
	private String normalaize(String value) {
		return value == null ? "" : value.trim().toLowerCase();
	}
	
	private String generateVerificationCode() {
	    StringBuilder code = new StringBuilder();

	    for (int i = 0; i < CODE_LENGHT; i++) {
	        int digit = random.nextInt(10);
	        code.append(digit);
	    }

	    return code.toString();
	}
	
}
