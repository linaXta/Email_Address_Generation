package lv.alina.emailgen.service;

import java.util.ArrayList;
import java.util.Optional;

import lv.alina.emailgen.models.Company;
import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.Symbol;
import lv.alina.emailgen.models.User;

public interface ICRUDCompanyService {
	
	ArrayList<Company> retrieveAllByUser(User user);

    ArrayList<Company> searchByUserAndText(User user, String searchText);

    Optional<Company> findById(Long id);

    boolean existsByUserAndCompanyName(User user, String companyName);

    Company add(User user, String companyName, String notes, MainEmail defaultMainEmail, Symbol symbolBeforeShortcode, Symbol symbolBeforeSequence, String shortCode) throws Exception;

    Company update(Company company, String companyName, String notes, MainEmail defaultMainEmail, Symbol symbolBeforeShortcode, Symbol symbolBeforeSequence, String shortCode) throws Exception;

    void delete(Company company);

}
