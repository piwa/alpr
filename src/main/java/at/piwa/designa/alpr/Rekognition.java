package at.piwa.designa.alpr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class Rekognition {

    private RekognitionClient rekognition;

    public Rekognition() {
        rekognition = RekognitionClient.create();
    }

    public List<TextDetection> detectTexts(ByteBuffer imageBytes) {
        SdkBytes lprImageBytes = SdkBytes.fromByteBuffer(imageBytes);
        Image lpImage = Image.builder().bytes(lprImageBytes).build();

        DetectTextRequest request = DetectTextRequest.builder().image(lpImage).build();


        try {
            DetectTextResponse result = rekognition.detectText(request);
            return result.textDetections();
        } catch(RekognitionException e) {
            log.error("EXCEPTION", e);
        }

        return Collections.<TextDetection>emptyList();
    }

}
