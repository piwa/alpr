package at.piwa.designa.alpr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class DetectedText {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    private String text;
    private Float confidence;


    public DetectedText(String text, Float confidence) {
        this.text = text;
        this.confidence = confidence;
    }
}
