package uk.gov.hmcts.reform.fpl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class DocmosisConfiguration {
    private final String url;
    private final String accessKey;

    public DocmosisConfiguration(@Value("${docmosis.tornado.url}") String url,
                                 @Value("${docmosis.tornado.key}") String accessKey) {
        this.url = url;
        this.accessKey = accessKey;
    }
}
