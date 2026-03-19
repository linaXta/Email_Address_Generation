package lv.alina.emailgen.models;

import java.time.LocalDateTime;

public class VerificationCodeData {
	
	private String code;
    private LocalDateTime expiresAt;
    
    public VerificationCodeData(String code, LocalDateTime expiresAt) {
        this.code = code;
        this.expiresAt = expiresAt;
    }
    
    public String getCode() {
        return code;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

}
