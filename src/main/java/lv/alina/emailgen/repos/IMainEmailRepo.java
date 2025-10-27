package lv.alina.emailgen.repos;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import lv.alina.emailgen.models.MainEmail;

public interface IMainEmailRepo extends CrudRepository<MainEmail, Long>{
    List<MainEmail> findByUserUserId(Long userId);
    MainEmail findByMainEmail(String mainEmail);

}
