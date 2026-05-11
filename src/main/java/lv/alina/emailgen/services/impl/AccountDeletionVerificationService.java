package lv.alina.emailgen.services.impl;

import org.springframework.stereotype.Service;

import lv.alina.emailgen.models.User;
import lv.alina.emailgen.models.enums.VerificationCodeStatus;
import lv.alina.emailgen.service.IAccountDeletionVerificationService;
import lv.alina.emailgen.service.IEmailService;
import lv.alina.emailgen.service.IVerificationCodeService;

@Service
public class AccountDeletionVerificationService implements IAccountDeletionVerificationService {
	
	private final IVerificationCodeService verificationCodeService;
    private final IEmailService emailService;

    public AccountDeletionVerificationService(
            IVerificationCodeService verificationCodeService,
            IEmailService emailService) {
        this.verificationCodeService = verificationCodeService;
        this.emailService = emailService;
    }

    @Override
    public void sendDeleteConfirmationCode(User user) throws Exception {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new Exception("User e-mail is required.");
        }

        String email = user.getEmail().trim().toLowerCase();

        String code = verificationCodeService.createAndStoreCode(email);

        emailService.sendVerificationCode( email, "Account deletion confirmation code",
        		"Your account deletion confirmation code is: " + code
        		+ "\n\nThis code is valid for 10 minutes. If you did not request account deletion, please ignore this email."
        );
    }

    @Override
    public boolean verifyDeleteConfirmationCode(String email, String code) throws Exception {
    	return verificationCodeService.getCodeStatus(email, code) == VerificationCodeStatus.VALID;
    }

}
