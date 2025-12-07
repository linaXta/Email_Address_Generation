package lv.alina.emailgen.repos;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.ShortCodeCounter;

public interface IShortCodeCounterRepo extends CrudRepository<ShortCodeCounter, Long> {

}
