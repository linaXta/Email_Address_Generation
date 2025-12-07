package lv.alina.emailgen.repos;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.Symbol;

public interface ISymbolRepo extends CrudRepository<Symbol, Long> {

}
