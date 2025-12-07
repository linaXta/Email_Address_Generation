package lv.alina.emailgen.repos;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.GeneratedEmail;

public interface IGeneratedEmailRepo extends CrudRepository<GeneratedEmail, Long>{

}
