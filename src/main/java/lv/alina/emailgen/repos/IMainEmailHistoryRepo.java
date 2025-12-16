package lv.alina.emailgen.repos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.MainEmailHistory;
import lv.alina.emailgen.models.MainEmailHistory.ActionType;


public interface IMainEmailHistoryRepo extends CrudRepository<MainEmailHistory, Long> {
	
    List<MainEmailHistory> findAllByMainEmailOrderByEventTimeDesc(MainEmail mainEmail);

    List<MainEmailHistory> findAllByMainEmailOrderByEventTimeAsc(MainEmail mainEmail);

    List<MainEmailHistory> findAllByMainEmailAndActionTypeOrderByEventTimeDesc(
            MainEmail mainEmail, ActionType actionType
    );

    List<MainEmailHistory> findAllByMainEmailAndEventTimeBetweenOrderByEventTimeDesc(
            MainEmail mainEmail, LocalDateTime from, LocalDateTime to
    );

    List<MainEmailHistory> findAllByMainEmailAndEventTimeBetweenOrderByEventTimeAsc(
            MainEmail mainEmail, LocalDateTime from, LocalDateTime to
    );

    long countByMainEmail(MainEmail mainEmail);

}
