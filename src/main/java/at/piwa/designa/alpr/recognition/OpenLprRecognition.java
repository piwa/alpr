package at.piwa.designa.alpr.recognition;

import at.piwa.designa.alpr.AbstractLicencePlateRecognition;
import at.piwa.designa.alpr.database.AlternativeLprBaseRepository;
import at.piwa.designa.alpr.database.DesignaLprRepository;
import at.piwa.designa.alpr.database.OpenAlprAlternativeLprRepository;
import at.piwa.designa.alpr.model.*;
import at.piwa.designa.alpr.model.openalpr.OpenAlpr;
import at.piwa.designa.alpr.model.openalpr.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class OpenLprRecognition extends AbstractLicencePlateRecognition {

    @Autowired
    private OpenAlprAlternativeLprRepository openAlprAlternativeLprRepository;
    @Autowired
    private DesignaLprRepository designaLprRepository;

    private int totalLicencePlateChecks = 0;
    private int licencePlatesRecognized = 0;
    private List<Long> recognitionDurations = new ArrayList<>();

    @Transactional
    public void run(String directory) throws Exception {

        totalLicencePlateChecks = 0;
        licencePlatesRecognized = 0;
        recognitionDurations = new ArrayList<>();

        Map<String, AlternativeLpr> alternativeLprSuccessful = new HashMap<>();
        openAlprAlternativeLprRepository.findAllByRecognized(RecognitionResult.positive).forEach(entry -> alternativeLprSuccessful.put(entry.getFilename(), entry));

        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            paths.filter(Files::isRegularFile).filter(path -> !alternativeLprSuccessful.containsKey(path.getFileName().toString())).map(Path::toFile).forEach(imageFile -> {

                recognitionTaskWatch.start(imageFile.getName());

                log.info("--- " + imageFile.getName() + " ---");
                try {
                    totalLicencePlateChecks = totalLicencePlateChecks + 1;

                    OpenAlpr openAlprResult = getOpenAlprResult(imageFile);

                    List<DesignaLpr> designaLprList = designaLprRepository.findByFilename(imageFile.getName());


                    if (openAlprResult.getResults().isEmpty()) {
                        recognitionTaskWatch.stop();
                        log.error("No text recognised");
                        writeNoTextResultToDatabase(imageFile, designaLprList, AlprApproachKind.OpenAlpr);
                    }
                    else {
                        List<DetectedTextHelper> detectedTexts = openAlprResult.getResults().stream().map(Result::getCandidates).flatMap(Collection::stream).map(DetectedTextHelper::new).collect(Collectors.toList());

                        if (designaLprList.isEmpty()) {
                            recognitionTaskWatch.stop();
                            printDesignaResultNotAvailable(detectedTexts, imageFile);
                            writeUnkownResultToDatabase(null, imageFile.getName(), new ArrayList<>(), recognitionTaskWatch.getLastTaskTimeMillis(), AlprApproachKind.OpenAlpr);

                        } else {
                            boolean plateFound = isPlateFound(designaLprList, detectedTexts);
                            if (!plateFound) {
                                recognitionTaskWatch.stop();
                                printLicencePlateNOTFoundInformation(imageFile, designaLprList, detectedTexts);
                                writeNegativeResultToDatabase(designaLprList.get(0), imageFile.getName(), detectedTexts, recognitionTaskWatch.getLastTaskTimeMillis(), AlprApproachKind.OpenAlpr);
                            }
                        }
                    }

                    log.info("----------------------------------------");
                } catch (IOException | InterruptedException ex) {
                    log.error("Exception", ex);
                }
                if(recognitionTaskWatch.isRunning()) {
                    recognitionTaskWatch.stop();
                }

                recognitionDurations.add(recognitionTaskWatch.getLastTaskTimeMillis());

            });
        }

        printFinalOutput(totalLicencePlateChecks, licencePlatesRecognized, recognitionDurations);
    }



    private OpenAlpr getOpenAlprResult(File imageFile) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();

        builder.command("sh", "-c", "alpr -jd -n 100 " + imageFile.getPath());

        builder.directory(new File(System.getProperty("user.home")));
        Process process = builder.start();

        StringBuilder stringBuilder = new StringBuilder();

        OpenAlprOutputListener openAlprOutputListener = new OpenAlprOutputListener(process.getInputStream(), stringBuilder::append);
        Executors.newSingleThreadExecutor().submit(openAlprOutputListener);
        process.waitFor();


        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(stringBuilder.toString(), OpenAlpr.class);
    }

    private boolean isPlateFound(List<DesignaLpr> designaLprList, List<DetectedTextHelper> recognizedTexts) {


        for (DetectedTextHelper recognizedText : recognizedTexts) {
            for (String convertedString : stringConverter(recognizedText.getLicencePlate())) {
                for (DesignaLpr designaLpr : designaLprList) {
                    if (designaLpr.getIdentifier().equals(convertedString)) {
                        recognitionTaskWatch.stop();
                        printLicencePlateFoundInformation(designaLpr, recognizedText.getLicencePlate(), recognizedText.getConfidence(), recognizedText.getLicencePlate());
                        writePositiveResultToDatabase(designaLpr, designaLpr.getFilename(), convertedString, recognizedText.getConfidence(), recognizedText.getLicencePlate(), recognizedTexts, recognitionTaskWatch.getLastTaskTimeMillis(), AlprApproachKind.OpenAlpr);

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

        convertedStrings.addAll(convertedStrings.stream().map(s -> textManipulator.replaceCharacterAndCreateCombinationBidirect(s, "2", "Z")).flatMap(Collection::stream).collect(Collectors.toList()));
        convertedStrings.addAll(convertedStrings.stream().map(s -> textManipulator.replaceCharacterAndCreateCombinationBidirect(s, "I", "T")).flatMap(Collection::stream).collect(Collectors.toList()));
        convertedStrings.addAll(convertedStrings.stream().map(s -> textManipulator.replaceCharacterAndCreateCombinationBidirect(s, "B", "8")).flatMap(Collection::stream).collect(Collectors.toList()));
        convertedStrings.addAll(convertedStrings.stream().map(s -> textManipulator.replaceCharacterAndCreateCombinationBidirect(s, "S", "5")).flatMap(Collection::stream).collect(Collectors.toList()));
        convertedStrings.addAll(convertedStrings.stream().map(s -> textManipulator.replaceCharacterAndCreateCombinationBidirect(s, "D", "0")).flatMap(Collection::stream).collect(Collectors.toList()));


        return convertedStrings;
    }




    private static class OpenAlprOutputListener implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        OpenAlprOutputListener(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }



}
