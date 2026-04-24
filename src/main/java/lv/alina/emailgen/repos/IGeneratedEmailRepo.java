package lv.alina.emailgen.repos;

import java.util.ArrayList;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.Company;
import lv.alina.emailgen.models.GeneratedEmail;
import lv.alina.emailgen.models.MainEmail;

public interface IGeneratedEmailRepo extends CrudRepository<GeneratedEmail, Long>{
	
	boolean existsByGeneratedEmailAddress(String generatedEmailAddress);
	
	boolean existsByCompanyAndGeneratedEmailAddress(Company company, String generatedEmailAddress);
	
	ArrayList<GeneratedEmail> findByMainEmail(MainEmail mainEmail);
	
	void deleteByMainEmail(MainEmail mainEmail);

}
