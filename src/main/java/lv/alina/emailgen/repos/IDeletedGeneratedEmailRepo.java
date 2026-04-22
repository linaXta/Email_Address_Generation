package lv.alina.emailgen.repos;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.DeletedGeneratedEmail;
import lv.alina.emailgen.models.MainEmail;

public interface IDeletedGeneratedEmailRepo extends CrudRepository<DeletedGeneratedEmail, Long> {
	
	boolean existsByEmailAddress(String emailAddress);
	
	boolean existsByMainEmailAndEmailAddress(MainEmail mainEmail, String emailAddress);
	
	void deleteByMainEmail(MainEmail mainEmail);

}
