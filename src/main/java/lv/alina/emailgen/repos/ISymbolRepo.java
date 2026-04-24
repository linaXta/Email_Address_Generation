package lv.alina.emailgen.repos;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.Symbol;
import lv.alina.emailgen.models.User;

public interface ISymbolRepo extends CrudRepository<Symbol, Long> {
	
	boolean existsByUserAndSymbol(User user, String symbol);
	
	Symbol findByUserAndSymbol(User user, String symbol);
	
	void deleteByUser(User user);

}
