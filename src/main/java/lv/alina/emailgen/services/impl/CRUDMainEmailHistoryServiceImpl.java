package lv.alina.emailgen.services.impl;

import org.springframework.stereotype.Service;

import lv.alina.emailgen.models.MainEmailHistory;
import lv.alina.emailgen.repos.IMainEmailHistoryRepo;
import lv.alina.emailgen.service.ICRUDMainEmailHistoryService;

@Service
public class CRUDMainEmailHistoryServiceImpl implements ICRUDMainEmailHistoryService{
	private final IMainEmailHistoryRepo mainEmailHistoryRepo;

    public CRUDMainEmailHistoryServiceImpl(IMainEmailHistoryRepo mainEmailHistoryRepo) {
        this.mainEmailHistoryRepo = mainEmailHistoryRepo;
    }

    @Override
    public MainEmailHistory add(MainEmailHistory history) {
        return mainEmailHistoryRepo.save(history);
    }

}
