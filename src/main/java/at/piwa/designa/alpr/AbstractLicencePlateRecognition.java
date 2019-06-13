package at.piwa.designa.alpr;

import at.piwa.designa.alpr.database.DesignaLprRepository;
import at.piwa.designa.alpr.model.*;
import at.piwa.designa.alpr.recognition.DetectedTextHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractLicencePlateRecognition {

    @Autowired
    private DesignaLprRepository designaLprRepository;

    protected StopWatch recognitionTaskWatch = new StopWatch("RecognitionTask");


    protected void writeResultToDatabase(DesignaLpr designaLpr, String filename, String licencePlate, Float lpConfidence, String lpDetectedText, List<DetectedTextHelper> recognizedTextHelpers, Long duration, AlprApproachKind alprApproachKind, RecognitionResult recognized) {

        List<DetectedText> detectedTexts = recognizedTextHelpers.stream().map(DetectedTextHelper::getDetectedText).collect(Collectors.toList());

        AlternativeLpr alternativeLpr;
        if(alprApproachKind.equals(AlprApproachKind.AWS)) {
            alternativeLpr = new AwsAlternativeLpr();
        }
        else {
            alternativeLpr = new OpenAlprAlternativeLpr();
        }


        alternativeLpr.setFilename(filename);
        alternativeLpr.setLicencePlateConfidence(lpConfidence);
        alternativeLpr.setLicencePlate(licencePlate);
        alternativeLpr.setLicencePlateDetectedText(lpDetectedText);
        alternativeLpr.setRecognized(recognized);
        alternativeLpr.setAllDetectedTexts(detectedTexts);
        alternativeLpr.setRecognitionDuration(duration);

        if(designaLpr != null) {
            alternativeLpr.setDesignaLpr(designaLprRepository.findById(designaLpr.getId()).orElse(null));
            designaLpr.getOtherSuccessfulRecognitions().add(alternativeLpr);
            designaLprRepository.save(designaLpr);
        }
    }

    protected void printLicencePlateNOTFoundInformation(File imageFile, List<DesignaLpr> designaLprList, List<DetectedTextHelper> detectedTexts) {
        log.error("Plate not found. Filename=" + imageFile.getName());
        designaLprList.forEach(designaLpr -> log.error("    DesignaLpr: licencePlate=" + designaLpr.getIdentifier() + ", licencePlateConfidence=" + designaLpr.getConfidence()));
        log.error("AlternativeLpr: ");
        detectedTexts.forEach(detectedText -> log.error("    licencePlate=" + detectedText.getLicencePlate() + ", licencePlateConfidence=" + detectedText.getConfidence()));
    }

    protected void printLicencePlateFoundInformation(DesignaLpr designaLpr, String detectedText, Float detectedTextConfidence, String convertedString) {
        log.info("Plate found. Filename=" + designaLpr.getFilename());
        log.info("    DesignaLpr: licencePlate=" + designaLpr.getIdentifier() + ", licencePlateConfidence=" + designaLpr.getConfidence());
        log.info("AlternativeLpr: licencePlate=" + convertedString + ", licencePlateConfidence=" + detectedTextConfidence + ", original text=" + detectedText);
    }

    protected void printDesignaResultNotAvailable(List<DetectedTextHelper> recognizedTexts, File imageFile) {
        log.error("Designa result not available");
        log.error("AlternativeLpr: ");
        recognizedTexts.forEach(detectedText -> log.error("    licencePlate=" + detectedText.getLicencePlate() + ", licencePlateConfidence=" + detectedText.getConfidence()));
    }

    protected void printFinalOutput(int totalLicencePlateChecks, int licencePlatesRecognized, List<Long> recognitionDurations) {
        log.info("Checkign done.");
        log.info("Total amount: " + totalLicencePlateChecks);
        log.info("Successful: " + licencePlatesRecognized);

        SummaryStatistics stats = new SummaryStatistics();
        recognitionDurations.forEach(stats::addValue);
        log.info("Duration mean (ms): " + stats.getMean());
        log.info("Duration std (ms): " + stats.getStandardDeviation());
        log.info("Duration total (sec): " + stats.getSum() / 100);

    }

    protected void writePositiveResultToDatabase(DesignaLpr designaLpr, String filename, String licencePlate, Float lpConfidence, String lpDetectedText, List<DetectedTextHelper> recognizedTexts, Long recognitionDuration, AlprApproachKind alprApproachKind) {
        writeResultToDatabase(designaLpr, filename, licencePlate, lpConfidence, lpDetectedText, recognizedTexts, recognitionDuration, alprApproachKind, RecognitionResult.positive);
    }

    protected void writeNegativeResultToDatabase(DesignaLpr designaLpr, String filename, List<DetectedTextHelper> recognizedTexts, Long recognitionDuration, AlprApproachKind alprApproachKind) {
        writeResultToDatabase(designaLpr, filename, "", 0.0f, "", recognizedTexts, recognitionDuration, alprApproachKind, RecognitionResult.negative);
    }

    protected void writeUnkownResultToDatabase(DesignaLpr designaLpr, String filename, List<DetectedTextHelper> recognizedTexts, Long recognitionDuration, AlprApproachKind alprApproachKind) {
        writeResultToDatabase(designaLpr, filename, "", 0.0f, "", recognizedTexts, recognitionDuration, alprApproachKind, RecognitionResult.unkown);
    }

    protected void writeNoTextResultToDatabase(File imageFile, List<DesignaLpr> designaLprList, AlprApproachKind alprApproachKind) {
        if(designaLprList.isEmpty()) {
            writeNegativeResultToDatabase(null, imageFile.getName(), new ArrayList<>(), recognitionTaskWatch.getLastTaskTimeMillis(), alprApproachKind);
        }
        else {
            writeNegativeResultToDatabase(designaLprList.get(0), imageFile.getName(), new ArrayList<>(), recognitionTaskWatch.getLastTaskTimeMillis(), alprApproachKind);
        }
    }
}
