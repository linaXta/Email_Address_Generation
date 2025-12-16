package lv.alina.emailgen.repos;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.Company;
import lv.alina.emailgen.models.GeneratedEmail;

public interface IGeneratedEmailRepo extends CrudRepository<GeneratedEmail, Long>{
	
	boolean existsByEmail(String email);
	
	boolean existsByCompanyAndEmail(String email, Company company);

}
