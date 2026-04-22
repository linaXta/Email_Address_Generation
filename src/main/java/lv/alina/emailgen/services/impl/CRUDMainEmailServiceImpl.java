package lv.alina.emailgen.services.impl;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lv.alina.emailgen.models.GeneratedEmail;
import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.repos.IDeletedGeneratedEmailRepo;
import lv.alina.emailgen.repos.IGeneratedEmailRepo;
import lv.alina.emailgen.repos.IMainEmailHistoryRepo;
import lv.alina.emailgen.repos.IMainEmailRepo;
import lv.alina.emailgen.service.ICRUDMainEmailService;

@Service
public class CRUDMainEmailServiceImpl implements ICRUDMainEmailService{
	
	private final IMainEmailRepo mainEmailRepo;
	private final IGeneratedEmailRepo generatedEmailRepo;
    private final IDeletedGeneratedEmailRepo deletedGeneratedEmailRepo;
    private final IMainEmailHistoryRepo mainEmailHistoryRepo;
    
	public CRUDMainEmailServiceImpl(IMainEmailRepo mainEmailRepo, IGeneratedEmailRepo generatedEmailRepo, IDeletedGeneratedEmailRepo deletedGeneratedEmailRepo, IMainEmailHistoryRepo mainEmailHistoryRepo) {
        this.mainEmailRepo = mainEmailRepo;
        this.generatedEmailRepo = generatedEmailRepo;
        this.deletedGeneratedEmailRepo = deletedGeneratedEmailRepo;
        this.mainEmailHistoryRepo = mainEmailHistoryRepo;
    }

    @Override
    public ArrayList<MainEmail> retrieveAllByUser(User user) throws Exception {
        ArrayList<MainEmail> userMainEmails = new ArrayList<>();
        if (user == null) {
            return userMainEmails;
        }

        mainEmailRepo.findAll().forEach(mainEmail -> {
            if (mainEmail.getUser() != null && mainEmail.getUser().getUserId().equals(user.getUserId())) {
                userMainEmails.add(mainEmail);
            }
        });

        return userMainEmails;
    }

    @Override
    public ArrayList<MainEmail> searchByUserAndText(User user, String searchText) throws Exception {
        ArrayList<MainEmail> filteredMainEmails = new ArrayList<>();

        if (user == null) {
            return filteredMainEmails;
        }

        String searchValue = searchText;

        if (searchValue == null) {
            searchValue = "";
        } else {
            searchValue = searchValue.trim().toLowerCase();
        }

        for (MainEmail mainEmail : retrieveAllByUser(user)) {
            String currentEmail = mainEmail.getMainEmail() == null ? "" : mainEmail.getMainEmail();

            if (searchValue.isBlank() || currentEmail.toLowerCase().contains(searchValue)) {
                filteredMainEmails.add(mainEmail);
            }
        }

        return filteredMainEmails;
    }
    
    @Override
    public boolean existsExact(User user, String mainEmail) {
        return mainEmailRepo.existsByUserAndMainEmail(user, mainEmail);
    }
    
    @Override
    public MainEmail add (MainEmail mainEmail) {
        return mainEmailRepo.save(mainEmail);
    }
    
    @Override
    public Optional<MainEmail> findById(Long id) {
        return mainEmailRepo.findById(id);
    }
    
    @Override
    @Transactional
    public void deleteMainOnly(MainEmail mainEmail) {
        ArrayList<GeneratedEmail> generatedEmails = generatedEmailRepo.findByMainEmail(mainEmail);

        for (GeneratedEmail generatedEmail : generatedEmails) {
            generatedEmail.setMainEmail(null);
            generatedEmailRepo.save(generatedEmail);
        }

        deletedGeneratedEmailRepo.deleteByMainEmail(mainEmail);
        mainEmailHistoryRepo.deleteByMainEmail(mainEmail);
        mainEmailRepo.delete(mainEmail);
    }
    
    @Override
    @Transactional
    public void deleteWithGenerated(MainEmail mainEmail) {
    	ArrayList<GeneratedEmail> generatedEmails = generatedEmailRepo.findByMainEmail(mainEmail);

        for (GeneratedEmail generatedEmail : generatedEmails) {
            generatedEmailRepo.delete(generatedEmail);
        }

        deletedGeneratedEmailRepo.deleteByMainEmail(mainEmail);
        mainEmailHistoryRepo.deleteByMainEmail(mainEmail);
        mainEmailRepo.delete(mainEmail);
    }

}
