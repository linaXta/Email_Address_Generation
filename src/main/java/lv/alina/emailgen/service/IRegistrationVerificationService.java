package lv.alina.emailgen.service;

import lv.alina.emailgen.models.enums.VerificationCodeStatus;

public interface IRegistrationVerificationService {
	
	String createAndStoreCode(String email) throws Exception;

    //boolean isCodeValid(String email, String code);

    void removeCode(String email) throws Exception;
    
    boolean hasActiveCode(String email) throws Exception;
    
    boolean canResendCode(String email) throws Exception;
    
    VerificationCodeStatus getCodeStatus(String email, String code) throws Exception;
    
    int getResendCooldownSeconds();
    
    int getRemainingResendCooldownSeconds(String email)throws Exception;

}
