package lv.alina.emailgen.repos;

import org.springframework.data.repository.CrudRepository;
import lv.alina.emailgen.models.User;

public interface IUserRepo extends CrudRepository<User, Long>{

	User findByEmail(String email);
	
}
