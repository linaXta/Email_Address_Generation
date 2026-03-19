package lv.alina.emailgen.service;

public interface IRegistrationVerificationService {
	
	String createAndStoreCode(String email) throws Exception;

    boolean isCodeValid(String email, String code);

    void removeCode(String email) throws Exception;

}
