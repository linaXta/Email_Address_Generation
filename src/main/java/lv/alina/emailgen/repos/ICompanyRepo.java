package lv.alina.emailgen.repos;

import java.util.ArrayList;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.Company;
import lv.alina.emailgen.models.User;

public interface ICompanyRepo extends CrudRepository<Company, Long> {
	
	Company findByUserAndCompanyNameIgnoreCase(User user, String companyName);
	
	boolean existsByUserAndCompanyNameIgnoreCase(User user, String companyName);
	
	void deleteByUser(User user);
	
	ArrayList<Company> findByUser(User user);

}
