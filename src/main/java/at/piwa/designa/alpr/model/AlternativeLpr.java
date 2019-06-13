package at.piwa.designa.alpr.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

//@KeySpace("designa_lpr")
@Entity
@Data
@Inheritance
public abstract class AlternativeLpr {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    private String filename;

    @ManyToOne
    @JoinColumn
    private DesignaLpr designaLpr;

    private String licencePlate;

    private Float licencePlateConfidence;

    private String licencePlateDetectedText;

    private Long recognitionDuration;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "alternative_pr_id", nullable = false)
    private List<DetectedText> allDetectedTexts;

    @Enumerated(EnumType.STRING)
    private RecognitionResult recognized;

}
