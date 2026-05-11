package lv.alina.emailgen.service;

public interface IEmailService {
	
	void sendVerificationCode(String toEmail, String subject, String messageText) throws Exception;

}
