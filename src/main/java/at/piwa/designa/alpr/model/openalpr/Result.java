
package at.piwa.designa.alpr.model.openalpr;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "plate",
    "licencePlateConfidence",
    "matches_template",
    "plate_index",
    "region",
    "region_confidence",
    "processing_time_ms",
    "requested_topn",
    "coordinates",
    "candidates"
})
@Data
public class Result {

    @JsonProperty("plate")
    public String plate;
    @JsonProperty("confidence")
    public Double confidence;
    @JsonProperty("matches_template")
    public Integer matchesTemplate;
    @JsonProperty("plate_index")
    public Integer plateIndex;
    @JsonProperty("region")
    public String region;
    @JsonProperty("region_confidence")
    public Integer regionConfidence;
    @JsonProperty("processing_time_ms")
    public Double processingTimeMs;
    @JsonProperty("requested_topn")
    public Integer requestedTopn;
    @JsonProperty("coordinates")
    public List<Coordinate> coordinates = null;
    @JsonProperty("candidates")
    public List<Candidate> candidates = null;

}
