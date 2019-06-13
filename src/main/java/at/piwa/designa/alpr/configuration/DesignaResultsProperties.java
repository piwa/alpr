package at.piwa.designa.alpr.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties("designa")
@Data
public class DesignaResultsProperties {

    private List<String> results = new ArrayList<>();

}
