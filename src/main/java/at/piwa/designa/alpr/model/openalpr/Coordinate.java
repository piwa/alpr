
package at.piwa.designa.alpr.model.openalpr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "x",
    "y"
})
@Data
public class Coordinate {

    @JsonProperty("x")
    public Integer x;
    @JsonProperty("y")
    public Integer y;

}
