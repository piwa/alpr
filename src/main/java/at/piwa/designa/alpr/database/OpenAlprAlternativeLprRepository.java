package at.piwa.designa.alpr.database;

import at.piwa.designa.alpr.model.OpenAlprAlternativeLpr;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface OpenAlprAlternativeLprRepository extends AlternativeLprBaseRepository<OpenAlprAlternativeLpr> {


}
