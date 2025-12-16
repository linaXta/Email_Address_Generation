package lv.alina.emailgen.repos;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.ShortCodes;
import lv.alina.emailgen.models.User;

public interface IShortCodesRepo extends CrudRepository<ShortCodes, Long> {
	
	boolean existsByUserAndCode(User user, String shortCode);
	
	ShortCodes findByUserAndShortCode(User user, String shortCode);

}
