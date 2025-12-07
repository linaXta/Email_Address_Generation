package lv.alina.emailgen.repos;

import org.springframework.data.repository.CrudRepository;

import lv.alina.emailgen.models.DeletedGeneratedEmail;

public interface IDeletedGeneratedEmailRepo extends CrudRepository<DeletedGeneratedEmail, Long> {

}
