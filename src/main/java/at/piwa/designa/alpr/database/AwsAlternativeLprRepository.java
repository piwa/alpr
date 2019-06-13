package at.piwa.designa.alpr.database;

import at.piwa.designa.alpr.model.AwsAlternativeLpr;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface AwsAlternativeLprRepository extends AlternativeLprBaseRepository<AwsAlternativeLpr> {


}
