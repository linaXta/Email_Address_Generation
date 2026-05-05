package lv.alina.emailgen.services.impl;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import lv.alina.emailgen.models.Symbol;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.models.enums.DefaultSymbol;
import lv.alina.emailgen.repos.ICompanyRepo;
import lv.alina.emailgen.repos.ISymbolRepo;
import lv.alina.emailgen.service.ISymbolService;

@Service
public class CRUDSymbolServiceImpl implements ISymbolService {

    private final ISymbolRepo symbolRepo;
    private final ICompanyRepo companyRepo;

    public CRUDSymbolServiceImpl(ISymbolRepo symbolRepo, ICompanyRepo companyRepo) {
        this.symbolRepo = symbolRepo;
        this.companyRepo = companyRepo;
    }

    @Override
    public ArrayList<Symbol> getSymbolsForUser(User user) {
        ensureDefaultSymbols(user);
        return symbolRepo.findByUser(user);
    }

    @Override
    public void ensureDefaultSymbols(User user) {
        if (user == null) {
            return;
        }

        createDefaultIfMissing(user, DefaultSymbol.PLUS.getValue());
        createDefaultIfMissing(user, DefaultSymbol.MINUS.getValue());
    }

    private void createDefaultIfMissing(User user, String value) {
        if (!symbolRepo.existsByUserAndSymbol(user, value)) {
            Symbol symbol = new Symbol();
            symbol.setUser(user);
            symbol.setSymbol(value);
            symbolRepo.save(symbol);
        }
    }

    @Override
    public Symbol createSymbol(User user, String symbolValue) throws Exception {
        if (user == null) {
            throw new Exception("User is required");
        }

        if (symbolValue == null || symbolValue.isBlank()) {
            throw new Exception("Symbol is required");
        }

        String value = symbolValue.trim();

        if (DefaultSymbol.NONE.getValue().equals(value)) {
            throw new Exception("Empty symbol cannot be saved");
        }

        if (symbolRepo.existsByUserAndSymbol(user, value)) {
            throw new Exception("Symbol already exists");
        }

        Symbol symbol = new Symbol();
        symbol.setUser(user);
        symbol.setSymbol(value);

        return symbolRepo.save(symbol);
    }

    @Override
    public void deleteSymbol(Symbol symbol) throws Exception {
        if (symbol == null) {
            throw new Exception("Symbol is required");
        }

        if (DefaultSymbol.PLUS.getValue().equals(symbol.getSymbol())
                || DefaultSymbol.MINUS.getValue().equals(symbol.getSymbol())
                || DefaultSymbol.NONE.getValue().equals(symbol.getSymbol())) {
            throw new Exception("Default symbols cannot be deleted");
        }

        long usageCount = companyRepo.countBySymbolBeforeShortcodeOrSymbolBeforeSequence(symbol, symbol);

        if (usageCount > 0) {
            throw new Exception("Symbol is used and cannot be deleted");
        }

        symbolRepo.delete(symbol);
    }
}
