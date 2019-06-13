package at.piwa.designa.alpr.database;

import at.piwa.designa.alpr.model.DesignaLpr;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.stream.Stream;

public interface DesignaLprRepository extends CrudRepository<DesignaLpr, Integer> {

    List<DesignaLpr> findByFilename(String filename);

}
