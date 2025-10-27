package lv.alina.emailgen.repos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.MainEmailHistory;
import lv.alina.emailgen.models.MainEmailHistory.ActionType;


public interface IMainEmailHistoryRepo extends CrudRepository<MainEmailHistory, Long> {
	// Visi ieraksti: jaunākas -> vecākais
    List<MainEmailHistory> findAllByMainEmailOrderByUpdatedAtDesc(MainEmail mainEmail);

    // Visi ieraksti: vecākais -> jaunākais
    List<MainEmailHistory> findAllByMainEmailOrderByUpdatedAtAsc(MainEmail mainEmail);

    // Filtrs pēc darbības tipa (UPDATE/DELETE), jaunākais -> vecākais
    List<MainEmailHistory> findAllByMainEmailAndActionTypeOrderByUpdatedAtDesc(
            MainEmail mainEmail, ActionType actionType
    );

    // Perioda filtrs, jaunākais -> vecākais
    List<MainEmailHistory> findAllByMainEmailAndUpdatedAtBetweenOrderByUpdatedAtDesc(
            MainEmail mainEmail, LocalDateTime from, LocalDateTime to
    );

    // Perioda filtrs, vecākais -> jaunākais
    List<MainEmailHistory> findAllByMainEmailAndUpdatedAtBetweenOrderByUpdatedAtAsc(
            MainEmail mainEmail, LocalDateTime from, LocalDateTime to
    );

    long countByMainEmail(MainEmail mainEmail);// skaits

}
