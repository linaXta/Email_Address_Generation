package lv.alina.emailgen.repos;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.Company;
import lv.alina.emailgen.models.ShortCodeCounter;
import lv.alina.emailgen.models.ShortCodes;

public interface IShortCodeCounterRepo extends CrudRepository<ShortCodeCounter, Long> {
	
	void deleteByCompany(Company company);

    void deleteByShortCode(ShortCodes shortCode);

}
