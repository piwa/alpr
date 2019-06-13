package at.piwa.designa.alpr.recognition;

import at.piwa.designa.alpr.model.DetectedText;
import at.piwa.designa.alpr.model.openalpr.Candidate;
import lombok.AllArgsConstructor;
import lombok.Data;
import software.amazon.awssdk.services.rekognition.model.TextDetection;

@Data
@AllArgsConstructor
public class DetectedTextHelper {

    private String licencePlate;
    private Float confidence;

    public DetectedTextHelper(TextDetection textDetection) {
        this.licencePlate = textDetection.detectedText();
        this.confidence = textDetection.confidence();
    }

    public DetectedTextHelper(Candidate openAlprCandidate) {
        this.licencePlate = openAlprCandidate.getPlate();
        this.confidence = openAlprCandidate.getConfidence().floatValue();
    }

    public DetectedText getDetectedText() {
        return new DetectedText(licencePlate, confidence);
    }

}
