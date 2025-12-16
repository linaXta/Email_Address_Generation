package lv.alina.emailgen.repos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.MainEmailHistory;
import lv.alina.emailgen.models.MainEmailHistory.ActionType;


public interface IMainEmailHistoryRepo extends CrudRepository<MainEmailHistory, Long> {
	
    List<MainEmailHistory> findAllByMainEmailOrderByUpdatedAtDesc(MainEmail mainEmail);

    List<MainEmailHistory> findAllByMainEmailOrderByUpdatedAtAsc(MainEmail mainEmail);

    List<MainEmailHistory> findAllByMainEmailAndActionTypeOrderByUpdatedAtDesc(
            MainEmail mainEmail, ActionType actionType
    );

    List<MainEmailHistory> findAllByMainEmailAndUpdatedAtBetweenOrderByUpdatedAtDesc(
            MainEmail mainEmail, LocalDateTime from, LocalDateTime to
    );

    List<MainEmailHistory> findAllByMainEmailAndUpdatedAtBetweenOrderByUpdatedAtAsc(
            MainEmail mainEmail, LocalDateTime from, LocalDateTime to
    );

    long countByMainEmail(MainEmail mainEmail);

}
