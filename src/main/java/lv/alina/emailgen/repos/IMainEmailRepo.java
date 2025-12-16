package lv.alina.emailgen.repos;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.Company;
import lv.alina.emailgen.models.MainEmail;

public interface IMainEmailRepo extends CrudRepository<MainEmail, Long>{
	
	MainEmail findByEmail(String email);
    
	boolean existsByEmail(String email);
	
	boolean existsByCompanyAndEmail(String email, Company company);

}
