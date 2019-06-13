package at.piwa.designa.alpr.database;

import at.piwa.designa.alpr.model.AlternativeLpr;
import at.piwa.designa.alpr.model.RecognitionResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.List;

@NoRepositoryBean
public interface AlternativeLprBaseRepository<T extends AlternativeLpr> extends CrudRepository<T, Integer> {

    List<T> findAllByRecognized(RecognitionResult recognitionResult);

    List<T> findAllByRecognizedIsNot(RecognitionResult recognitionResult);

}
