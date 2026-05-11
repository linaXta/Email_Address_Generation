package lv.alina.emailgen.service;

import lv.alina.emailgen.models.User;

public interface IAccountDeletionVerificationService {
	void sendDeleteConfirmationCode(User user) throws Exception;

    boolean verifyDeleteConfirmationCode(String email, String code) throws Exception;

}
