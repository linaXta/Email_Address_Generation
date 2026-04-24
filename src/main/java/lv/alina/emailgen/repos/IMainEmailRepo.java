package lv.alina.emailgen.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.User;

public interface IMainEmailRepo extends CrudRepository<MainEmail, Long>{
	
	MainEmail findByUserAndMainEmail(User user, String mainEmail);
    
	boolean existsByUserAndMainEmailIgnoreCase(User user, String mainEmail);
	
	boolean existsByUserAndMainEmail(User user, String mainEmail);
	
	Optional<MainEmail> findById(Long id);
	
	List<MainEmail> findByUser(User user);
	
	void deleteByUser(User user);
	
}
