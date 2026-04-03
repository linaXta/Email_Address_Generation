package lv.alina.emailgen.services.impl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lv.alina.emailgen.models.VerificationCodeData;
import lv.alina.emailgen.models.enums.VerificationCodeStatus;
import lv.alina.emailgen.service.IRegistrationVerificationService;

@Service
public class RegistrationVerificationServiceImpl implements IRegistrationVerificationService{
	
	private static final int CODE_LENGHT = 6;
	private static final int EXPIRATION_IN_MINUTES = 10;
	private static final int RESEND_COOLDOWN_SECONDS = 120;

	
	private final Map<String, VerificationCodeData> verificationCodes = new ConcurrentHashMap<>();
	private final Random random = new Random();
	
	@Override
	public String createAndStoreCode(String email) throws Exception {
		String normalaizedEmail = normalaize(email);
		
		if (normalaizedEmail.isBlank()) {
			throw new Exception ("E-mail is required.");
		}
		
		String code = generateVerificationCode();
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiresAt = now.plusMinutes(EXPIRATION_IN_MINUTES);
		
		verificationCodes.put(normalaizedEmail, new VerificationCodeData(code, expiresAt, now));
		
		return code;
	}
	

//	@Override
//    public boolean isCodeValid(String email, String code) {
//    	String normalaizedEmail = normalaize(email);
//    	String normalizedCode = code == null ? "" : code.trim();
//    	
//    	if (normalaizedEmail.isBlank() || normalizedCode.isBlank()) {
//    		return false;
//    	}
//    	
//    	VerificationCodeData storedDate = verificationCodes.get(normalaizedEmail);
//    	
//    	if (storedDate == null) {
//    		return false;
//    	}
//    	
//    	if (storedDate.getExpiresAt().isBefore(LocalDateTime.now())) {
//    		verificationCodes.remove(normalaizedEmail);
//    		return false;
//    	}
//    	
//    	return storedDate.getCode().equals(normalizedCode);
//    }

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

	    VerificationCodeData data = verificationCodes.get(normalizedEmail);

	    if (data == null) {
	        return false;
	    }

	    if (data.getExpiresAt().isBefore(LocalDateTime.now())) {
	        verificationCodes.remove(normalizedEmail);
	        return false;
	    }

	    return true;
	}
	
	@Override
	public boolean canResendCode(String email) throws Exception {
	    String normalizedEmail = normalaize(email);

	    if (normalizedEmail.isBlank()) {
	        return false;
	    }

	    VerificationCodeData data = verificationCodes.get(normalizedEmail);

	    if (data == null) {
	        return true;
	    }

	    LocalDateTime lastSent = data.getLastSentAt();

	    if (lastSent == null) {
	        return true;
	    }

	    return lastSent.plusSeconds(RESEND_COOLDOWN_SECONDS).isBefore(LocalDateTime.now());
	}
	
	@Override
	public VerificationCodeStatus getCodeStatus(String email, String code) throws Exception {
	    String normalizedEmail = normalaize(email);
	    String normalizedCode; 
	    
	    if (code == null) {
	        normalizedCode = "";
	    } else {
	        normalizedCode = code.trim();
	    }

	    if (normalizedEmail.isBlank() || normalizedCode.isBlank()) {
	        return VerificationCodeStatus.INVALID;
	    }

	    VerificationCodeData storedData = verificationCodes.get(normalizedEmail);

	    if (storedData == null) {
	        return VerificationCodeStatus.NOT_FOUND;
	    }

	    if (storedData.getExpiresAt().isBefore(LocalDateTime.now())) {
	        verificationCodes.remove(normalizedEmail);
	        return VerificationCodeStatus.EXPIRED;
	    }

	    if (!storedData.getCode().equals(normalizedCode)) {
	        return VerificationCodeStatus.INVALID;
	    }

	    return VerificationCodeStatus.VALID;
	}
	
	@Override
	public int getResendCooldownSeconds() {
	    return RESEND_COOLDOWN_SECONDS;
	}
	
	@Override
	public int getRemainingResendCooldownSeconds(String email) throws Exception {
	    String normalizedEmail = normalaize(email);
	    
	    if(normalizedEmail.isBlank()) {
	    	return 0;
	    }

	    VerificationCodeData data = verificationCodes.get(normalizedEmail);

	    if (data == null) {
	        return 0;
	    }
	    
	    LocalDateTime lastSentTime = data.getLastSentAt();
	    
	    if (lastSentTime == null) {
	        return 0;
	    }

	    LocalDateTime nextAllowedTime = lastSentTime.plusSeconds(RESEND_COOLDOWN_SECONDS);
	    LocalDateTime now = LocalDateTime.now();

	    if (now.isAfter(nextAllowedTime)) {
	        return 0;
	    }
	    
	    long remainingSeconds = java.time.Duration.between(now, nextAllowedTime).getSeconds();

	    return (int) remainingSeconds;
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
