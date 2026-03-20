package lv.alina.emailgen.services.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lv.alina.emailgen.service.IEmailService;

@Service
public class EmailServiceImpl implements IEmailService {
	
	private final JavaMailSender mailSender;
	
	public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationCode(String toEmail, String code) throws Exception {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Your verification code");
        message.setText("Your email verification code is: " + code + "\n\n This code is valid for 10 minutes.");

        mailSender.send(message);
    }

}
