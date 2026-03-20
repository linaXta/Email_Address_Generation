package lv.alina.emailgen.models;

import java.time.LocalDateTime;

public class VerificationCodeData {
	
	private String code;
    private LocalDateTime expiresAt;
    private LocalDateTime lastSentAt;
    
    public VerificationCodeData(String code, LocalDateTime expiresAt, LocalDateTime lastSentAt ) {
        this.code = code;
        this.expiresAt = expiresAt;
        this.lastSentAt = lastSentAt;
    }
    
    public String getCode() {
        return code;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public LocalDateTime getLastSentAt() {
        return lastSentAt;
    }

}
