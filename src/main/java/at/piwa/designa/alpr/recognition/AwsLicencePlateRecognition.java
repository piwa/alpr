package at.piwa.designa.alpr.recognition;

import at.piwa.designa.alpr.AbstractLicencePlateRecognition;
import at.piwa.designa.alpr.Rekognition;
import at.piwa.designa.alpr.database.AlternativeLprBaseRepository;
import at.piwa.designa.alpr.database.AwsAlternativeLprRepository;
import at.piwa.designa.alpr.database.DesignaLprRepository;
import at.piwa.designa.alpr.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import software.amazon.awssdk.services.rekognition.model.TextDetection;
import software.amazon.awssdk.utils.IoUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class AwsLicencePlateRecognition extends AbstractLicencePlateRecognition {
    @Autowired
    private Rekognition rekognition;
    @Autowired
    private AwsAlternativeLprRepository awsAlternativeLprRepository;
    @Autowired
    private DesignaLprRepository designaLprRepository;

    private int totalLicencePlateChecks = 0;
    private int licencePlatesRecognized = 0;
    private List<Long> recognitionDurations = new ArrayList<>();

    private StopWatch recognitionTaskWatch = new StopWatch("RecognitionTask");

    @Transactional
    public void run(String directory) throws Exception {

        totalLicencePlateChecks = 0;
        licencePlatesRecognized = 0;
        recognitionDurations = new ArrayList<>();

        Map<String, AlternativeLpr> alternativeLprSuccessful = new HashMap<>();
        awsAlternativeLprRepository.findAllByRecognized(RecognitionResult.positive).forEach(entry -> alternativeLprSuccessful.put(entry.getFilename(), entry));

        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            paths.filter(Files::isRegularFile).filter(path -> !alternativeLprSuccessful.containsKey(path.getFileName().toString())).map(Path::toFile).forEach(imageFile -> {
                recognitionTaskWatch.start(imageFile.getName());

                log.info("--- " + imageFile.getName() + " ---");
                try (InputStream inputStream = new FileInputStream(imageFile)) {

                    totalLicencePlateChecks = totalLicencePlateChecks + 1;

                    ByteBuffer imageBytes = ByteBuffer.wrap(IoUtils.toByteArray(inputStream));
                    List<TextDetection> detectedTexts = rekognition.detectTexts(imageBytes);

                    List<DesignaLpr> designaLprList = designaLprRepository.findByFilename(imageFile.getName());

                    if (detectedTexts.isEmpty()) {
                        recognitionTaskWatch.stop();
                        log.error("No text recognised");
                        writeNoTextResultToDatabase(imageFile, designaLprList, AlprApproachKind.AWS);
                    }
                    else if (designaLprList.isEmpty()) {
                        recognitionTaskWatch.stop();
                        List<DetectedTextHelper> recognizedTexts = detectedTexts.stream().map(DetectedTextHelper::new).collect(Collectors.toList());
                        printDesignaResultNotAvailable(recognizedTexts, imageFile);
                        writeUnkownResultToDatabase(null, imageFile.getName(), recognizedTexts, recognitionTaskWatch.getLastTaskTimeMillis(), AlprApproachKind.AWS);

                    } else {
                        boolean plateFound = isPlateFound(detectedTexts, designaLprList);
                        if (!plateFound) {
                            recognitionTaskWatch.stop();
                            List<DetectedTextHelper> recognizedTexts = detectedTexts.stream().map(DetectedTextHelper::new).collect(Collectors.toList());
                            printLicencePlateNOTFoundInformation(imageFile, designaLprList, recognizedTexts);
                            writeNegativeResultToDatabase(designaLprList.get(0), imageFile.getName(), recognizedTexts, recognitionTaskWatch.getLastTaskTimeMillis(), AlprApproachKind.AWS);
                        }
                    }
                    log.info("----------------------------------------");
                } catch (IOException e) {
                    log.error("EXCEPTION", e);
                }
                if(recognitionTaskWatch.isRunning()) {
                    recognitionTaskWatch.stop();
                }
                recognitionDurations.add(recognitionTaskWatch.getLastTaskTimeMillis());
            });

        }

        printFinalOutput(totalLicencePlateChecks, licencePlatesRecognized, recognitionDurations);

    }



    private boolean isPlateFound(List<TextDetection> detectedTexts, List<DesignaLpr> designaLprStream) {
        for (TextDetection detectedText : detectedTexts) {
            for (String convertedString : stringConverter(detectedText.detectedText())) {
                for (DesignaLpr designaLpr : designaLprStream) {
                    if (designaLpr.getIdentifier().equals(convertedString)) {
                        recognitionTaskWatch.stop();
                        printLicencePlateFoundInformation(designaLpr, detectedText.detectedText(), detectedText.confidence(), convertedString);
                        List<DetectedTextHelper> recognizedTexts = detectedTexts.stream().map(DetectedTextHelper::new).collect(Collectors.toList());
                        writePositiveResultToDatabase(designaLpr, designaLpr.getFilename(), convertedString, detectedText.confidence(), detectedText.detectedText(), recognizedTexts, recognitionTaskWatch.getLastTaskTimeMillis(), AlprApproachKind.AWS);

                        licencePlatesRecognized = licencePlatesRecognized + 1;

                        return true;
                    }
                }
            }
        }
        return false;
    }


    private List<String> stringConverter(String detectedText) {

        TextManipulator textManipulator = new TextManipulator();

        List<String> convertedStrings = new ArrayList<>();

        convertedStrings.add(detectedText);

        convertedStrings.add(detectedText.replaceAll("[\\- \\'\\ \\.]", ""));

        convertedStrings.addAll(convertedStrings.stream().map(s -> textManipulator.replaceCharacterAndCreateCombinationBidirect(s, "0", "O")).flatMap(Collection::stream).collect(Collectors.toList()));
        convertedStrings.addAll(convertedStrings.stream().map(s -> textManipulator.replaceCharacterAndCreateCombinationBidirect(s, "S", "5")).flatMap(Collection::stream).collect(Collectors.toList()));
        convertedStrings.addAll(convertedStrings.stream().map(s -> textManipulator.replaceCharacterAndCreateCombinationBidirect(s, "1", "T")).flatMap(Collection::stream).collect(Collectors.toList()));
        convertedStrings.addAll(convertedStrings.stream().map(s -> textManipulator.replaceCharacterAndCreateCombinationBidirect(s, "C", "0")).flatMap(Collection::stream).collect(Collectors.toList()));


        List<String> tempStrings = new ArrayList<>();
        convertedStrings.forEach(s -> tempStrings.addAll(textManipulator.removeParticularCharacter(s, "4")));
        convertedStrings.addAll(tempStrings);


        return convertedStrings;
    }


}

