
package at.piwa.designa.alpr.model.openalpr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "plate",
    "licencePlateConfidence",
    "matches_template"
})
@Data
public class Candidate {

    @JsonProperty("plate")
    public String plate;
    @JsonProperty("confidence")
    public Double confidence;
    @JsonProperty("matches_template")
    public Integer matchesTemplate;

}
