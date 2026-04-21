package lv.alina.emailgen.service;

import java.util.ArrayList;
import java.util.Optional;

import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.User;

public interface ICRUDMainEmailService extends ICRUDBase<MainEmail> {
	
	ArrayList<MainEmail> retrieveAllByUser(User user) throws Exception ;

    ArrayList<MainEmail> searchByUserAndText(User user, String searchText) throws Exception;
    
    boolean existsExact(User user, String mainEmail);
    
    MainEmail add(MainEmail mainEmail);

	Optional<MainEmail> findById(Long id);

}
