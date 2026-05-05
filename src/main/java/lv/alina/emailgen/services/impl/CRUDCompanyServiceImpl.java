package lv.alina.emailgen.services.impl;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lv.alina.emailgen.models.Company;
import lv.alina.emailgen.models.GeneratedEmail;
import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.MainEmailHistory;
import lv.alina.emailgen.models.ShortCodes;
import lv.alina.emailgen.models.Symbol;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.repos.ICompanyRepo;
import lv.alina.emailgen.repos.IGeneratedEmailRepo;
import lv.alina.emailgen.repos.IMainEmailHistoryRepo;
import lv.alina.emailgen.repos.IShortCodesRepo;
import lv.alina.emailgen.service.ICRUDCompanyService;

@Service
public class CRUDCompanyServiceImpl implements ICRUDCompanyService{
	
	private final ICompanyRepo companyRepo;
	private final IGeneratedEmailRepo generatedEmailRepo;
	private final IMainEmailHistoryRepo mainEmailHistoryRepo;
	private final IShortCodesRepo shortCodesRepo;

    public CRUDCompanyServiceImpl(ICompanyRepo companyRepo, IGeneratedEmailRepo generatedEmailRepo, 
    		IMainEmailHistoryRepo mainEmailHistoryRepo, IShortCodesRepo shortCodesRepo) {
        this.companyRepo = companyRepo;
        this.generatedEmailRepo = generatedEmailRepo;
        this.mainEmailHistoryRepo = mainEmailHistoryRepo;
        this.shortCodesRepo = shortCodesRepo;
    }

    @Override
    public ArrayList<Company> retrieveAllByUser(User user) {
        if (user == null) {
            return new ArrayList<>();
        }
        return companyRepo.findByUser(user);
    }

    @Override
    public ArrayList<Company> searchByUserAndText(User user, String searchText) {
        ArrayList<Company> filteredCompanies = new ArrayList<>();

        if (user == null) {
            return filteredCompanies;
        }

        String searchValue = searchText;

        if (searchValue == null) {
            searchValue = "";
        } else {
            searchValue = searchValue.trim().toLowerCase();
        }

        for (Company company : retrieveAllByUser(user)) {
            String companyName = company.getCompanyName();

            if (searchValue.isBlank() || companyName.toLowerCase().contains(searchValue)) {
                filteredCompanies.add(company);
            }
        }
        return filteredCompanies;
    }

    @Override
    public Optional<Company> findById(Long id) {
        return companyRepo.findById(id);
    }

    @Override
    public boolean existsByUserAndCompanyName(User user, String companyName) {
        if (user == null || companyName == null || companyName.isBlank()) {
            return false;
        }
        return companyRepo.existsByUserAndCompanyNameIgnoreCase(user, companyName.trim());
    }

    @Override
    public Company add(User user, String companyName, String notes, MainEmail defaultMainEmail, 
    		Symbol symbolBeforeShortcode, Symbol symbolBeforeSequence, String shortCode) throws Exception {
        if (user == null) {
            throw new Exception("User is required");
        }

        if (companyName == null || companyName.isBlank()) {
            throw new Exception("Company name is requred");
        }

        String trimmedCompanyName = companyName.trim();

        if (existsByUserAndCompanyName(user, trimmedCompanyName)) {
            throw new Exception("Company with this name already exists");
        }

        Company company = new Company();
        company.setUser(user);
        company.setCompanyName(trimmedCompanyName);
        company.setNotes(notes);
        company.setDefaultMainEmail(defaultMainEmail);
        company.setSymbolBeforeShortcode(symbolBeforeShortcode);
        company.setSymbolBeforeSequence(symbolBeforeSequence);

        Company savedCompany = companyRepo.save(company);

        ShortCodes currentShortCode = createShortCodeIfNeeded(user, savedCompany, shortCode);
        savedCompany.setCurrentShortCode(currentShortCode);

        return companyRepo.save(savedCompany);
    }

    @Override
    public Company update(Company company, String companyName, String notes, MainEmail defaultMainEmail,
    		Symbol symbolBeforeShortcode, Symbol symbolBeforeSequence, String shortCode) throws Exception {
        if (company == null) {
            throw new Exception("Company is requred");
        }

        if (companyName == null || companyName.isBlank()) {
            throw new Exception("Company name is requred");
        }

        String trimmedCompanyName = companyName.trim();

        Company existingCompany = companyRepo.findByUserAndCompanyNameIgnoreCase(company.getUser(), trimmedCompanyName);

        if (existingCompany != null && !existingCompany.getCompanyId().equals(company.getCompanyId())) {
            throw new Exception("Company with this name already exists");
        }

        company.setCompanyName(trimmedCompanyName);
        company.setNotes(notes);
        company.setDefaultMainEmail(defaultMainEmail);
        company.setSymbolBeforeShortcode(symbolBeforeShortcode);
        company.setSymbolBeforeSequence(symbolBeforeSequence);

        ShortCodes currentShortCode = createShortCodeIfNeeded(company.getUser(), company, shortCode);
        company.setCurrentShortCode(currentShortCode);

        return companyRepo.save(company);
    }
    
    private ShortCodes createShortCodeIfNeeded(User user, Company company, String shortCode) {
        if (shortCode == null || shortCode.isBlank()) {
            return null;
        }

        String value = shortCode.trim();

        ShortCodes existingShortCode = shortCodesRepo.findByUserAndShortCode(user, value);

        if (existingShortCode != null) {
            existingShortCode.setCompany(company);
            return shortCodesRepo.save(existingShortCode);
        }

        ShortCodes newShortCode = new ShortCodes();
        newShortCode.setUser(user);
        newShortCode.setCompany(company);
        newShortCode.setShortCode(value);

        return shortCodesRepo.save(newShortCode);
    }

    @Override
    @Transactional
    public void delete(Company company) {
    	ArrayList<GeneratedEmail> generatedEmails = generatedEmailRepo.findByCompany(company);

        for (GeneratedEmail generatedEmail : generatedEmails) {
            MainEmail mainEmail = generatedEmail.getMainEmail();

            if (mainEmail != null) {
                MainEmailHistory history = new MainEmailHistory();
                history.setMainEmail(mainEmail);
                history.setGeneratedEmail(generatedEmail);
                history.setOldValue(generatedEmail.getGeneratedEmailAddress());
                history.setNewValue(null);
                history.setActionType(MainEmailHistory.ActionType.DELETED_GENERATION);

                mainEmailHistoryRepo.save(history);
            }

            generatedEmailRepo.delete(generatedEmail);
        }

        companyRepo.delete(company);
    }	

}
