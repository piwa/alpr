package at.piwa.designa.alpr.recognition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LicencePlateRecognitionStart implements CommandLineRunner {

    @Autowired
    private AwsLicencePlateRecognition awsLicencePlateRecognition;
    @Autowired
    private OpenLprRecognition openLprRecognition;

    @Override
    public void run(String... args) throws Exception {

//        awsLicencePlateRecognition.run("/Users/pwaibel/Documents/work/designa/lpr_NY/to30/");
        openLprRecognition.run("/Users/pwaibel/Documents/work/designa/lpr_NY/to30/");
    }
}