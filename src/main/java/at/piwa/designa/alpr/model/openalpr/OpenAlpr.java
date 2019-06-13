
package at.piwa.designa.alpr.model.openalpr;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "version",
    "data_type",
    "epoch_time",
    "img_width",
    "img_height",
    "processing_time_ms",
    "regions_of_interest",
    "results"
})
@Data
public class OpenAlpr {

    @JsonProperty("version")
    public Integer version;
    @JsonProperty("data_type")
    public String dataType;
    @JsonProperty("epoch_time")
    public Long epochTime;
    @JsonProperty("img_width")
    public Integer imgWidth;
    @JsonProperty("img_height")
    public Integer imgHeight;
    @JsonProperty("processing_time_ms")
    public Double processingTimeMs;
    @JsonProperty("regions_of_interest")
    public List<RegionsOfInterest> regionsOfInterest = null;
    @JsonProperty("results")
    public List<Result> results = null;

}
