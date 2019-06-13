package at.piwa.designa.alpr.database;

import at.piwa.designa.alpr.model.AlternativeLpr;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface AlternativeLprRepository extends AlternativeLprBaseRepository<AlternativeLpr> {
}
