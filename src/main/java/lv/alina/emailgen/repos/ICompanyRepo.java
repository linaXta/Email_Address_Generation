package lv.alina.emailgen.repos;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.Company;

public interface ICompanyRepo extends CrudRepository<Company, Long> {
	
	Company findByNameIgnoreCase(String name);
	
	Company findByShortCode(String shortCode);
	
	boolean existsByShortCode(String shortCode);
	
	boolean existsByNameIgnoreCase(String name);

}
