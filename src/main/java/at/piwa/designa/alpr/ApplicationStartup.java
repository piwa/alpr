package at.piwa.designa.alpr;

import at.piwa.designa.alpr.configuration.DesignaResultsProperties;
import at.piwa.designa.alpr.database.AlternativeLprBaseRepository;
import at.piwa.designa.alpr.database.AwsAlternativeLprRepository;
import at.piwa.designa.alpr.database.DesignaLprRepository;
import at.piwa.designa.alpr.database.OpenAlprAlternativeLprRepository;
import at.piwa.designa.alpr.model.*;
import at.piwa.designa.alpr.model.csv.DesignaLprCsvDTO;
import at.piwa.designa.alpr.model.openalpr.OpenAlpr;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class ApplicationStartup implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private DesignaResultsProperties designaResultsProperties;
    @Autowired
    private ModelMapper modelMapper;
    //    @Autowired
//    private KeyValueTemplate keyValueTemplate;
    @Autowired
    private DesignaLprRepository designaLprRepository;
    @Autowired
    private AwsAlternativeLprRepository awsAlternativeLprRepository;
    @Autowired
    private OpenAlprAlternativeLprRepository openAlprAlternativeLprRepository;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {

        for (String designaLprResultPath : designaResultsProperties.getResults()) {
            try {
                List<DesignaLprCsvDTO> desginaLprCsvDTO = new CsvToBeanBuilder<DesignaLprCsvDTO>(new FileReader(designaLprResultPath)).withType(DesignaLprCsvDTO.class)
                        .withSeparator(';').withEscapeChar('$').withIgnoreLeadingWhiteSpace(true).withSkipLines(1)
                        .build().parse();

                desginaLprCsvDTO.forEach(dto -> {
                    DesignaLpr designaLpr = modelMapper.map(dto, DesignaLpr.class);

                    List<DesignaLpr> designaLprInDb = designaLprRepository.findByFilename(designaLpr.getFilename());

                    if (designaLprInDb.isEmpty()) {
                        designaLprRepository.save(designaLpr);
                    }

                });

            } catch (FileNotFoundException e) {
                log.error("Exception", e);
            }
        }

        List<AwsAlternativeLpr> notSucceededAws = awsAlternativeLprRepository.findAllByRecognizedIsNot(RecognitionResult.positive);
        List<OpenAlprAlternativeLpr> notSucceededOpenAlpr = openAlprAlternativeLprRepository.findAllByRecognizedIsNot(RecognitionResult.positive);

        awsAlternativeLprRepository.deleteAll(notSucceededAws);
        openAlprAlternativeLprRepository.deleteAll(notSucceededOpenAlpr);

    }


}
