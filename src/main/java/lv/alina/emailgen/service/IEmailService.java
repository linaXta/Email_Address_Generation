package lv.alina.emailgen.service;

public interface IEmailService {
	
	void sendVerificationCode(String toEmail, String code) throws Exception;

}
