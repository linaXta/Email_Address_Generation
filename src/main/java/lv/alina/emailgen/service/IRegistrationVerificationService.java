package lv.alina.emailgen.service;

public interface IRegistrationVerificationService {
	
	String createAndStoreCode(String email) throws Exception;

    boolean isCodeValid(String email, String code) throws Exception;

    void removeCode(String email) throws Exception;

}
