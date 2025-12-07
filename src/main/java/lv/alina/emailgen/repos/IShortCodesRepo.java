package lv.alina.emailgen.repos;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.ShortCodes;

public interface IShortCodesRepo extends CrudRepository<ShortCodes, Long> {

}
