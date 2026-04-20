package lv.alina.emailgen.services.impl;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.repos.IMainEmailRepo;
import lv.alina.emailgen.service.ICRUDMainEmailService;

@Service
public class CRUDMainEmailServiceImpl implements ICRUDMainEmailService{
	
	private final IMainEmailRepo mainEmailRepo;
	
	public CRUDMainEmailServiceImpl(IMainEmailRepo mainEmailRepo) {
        this.mainEmailRepo = mainEmailRepo;
    }
	
	@Override
    public ArrayList<MainEmail> retrieveAll() throws Exception {
        ArrayList<MainEmail> allMainEmails = new ArrayList<>();
        mainEmailRepo.findAll().forEach(allMainEmails::add);
        return allMainEmails;
    }

    @Override
    public MainEmail retrieveById(Long id) throws Exception {
        return mainEmailRepo.findById(id)
                .orElseThrow(() -> new Exception("Main e-mail not found with id = " + id));
    }

    @Override
    public void deleteById(Long id) throws Exception {
        if (!mainEmailRepo.existsById(id)) {
            throw new Exception("Main e-mail not found with id = " + id);
        }

        mainEmailRepo.deleteById(id);
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

}
