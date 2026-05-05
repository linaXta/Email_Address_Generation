package lv.alina.emailgen.service;

import java.util.ArrayList;

import lv.alina.emailgen.models.Symbol;
import lv.alina.emailgen.models.User;

public interface ISymbolService {
	
	ArrayList<Symbol> getSymbolsForUser(User user);

    Symbol createSymbol(User user, String symbolValue) throws Exception;

    void deleteSymbol(Symbol symbol) throws Exception;

    void ensureDefaultSymbols(User user);

}
