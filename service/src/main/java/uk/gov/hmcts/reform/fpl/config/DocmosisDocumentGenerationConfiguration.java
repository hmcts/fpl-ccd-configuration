package uk.gov.hmcts.reform.fpl.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocmosisDocumentGenerationConfiguration {

    @Bean
    public DocmosisConfig docmosisConfig(@Value("${docmosis.tornado.url}") String url,
                                         @Value("${docmosis.tornado.key}") String accessKey) {
        return new DocmosisConfig(url, accessKey);
    }

    @Data
    @AllArgsConstructor
    public static class DocmosisConfig {
        private final String url;
        private final String accessKey;
    }
}
