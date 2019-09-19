package uk.gov.hmcts.reform.fpl.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocmosisDocumentGenerationConfiguration {

    public DocmosisConfig docmosisConfig(String url, String accessKey) {
        return new DocmosisConfig(url, accessKey);
    }

    @Data
    @AllArgsConstructor
    public static class DocmosisConfig {
        private final String url;
        private final String accessKey;
    }
}
