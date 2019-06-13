package at.piwa.designa.alpr.model;

import lombok.*;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.List;

//@KeySpace("designa_lpr")
@Entity
@Data
public class DesignaLpr {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    private String filename;

    private Integer laneId;

    private DateTime timestamp;

    private String jurisdiction;

    private Double jurisdictionConfidence;

    private String identifier;

    private Double confidence;

    private String licensePlateImagePath;


    @OneToMany(mappedBy = "designaLpr", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<AlternativeLpr> otherSuccessfulRecognitions ;


}
