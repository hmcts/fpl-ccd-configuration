package uk.gov.hmcts.reform.fpl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class DocumentConfiguration {
    private final String documentHostUrl;

    public DocumentConfiguration(@Value("${document_management.dm_host_url}") String documentHostUrl) {
        this.documentHostUrl = documentHostUrl;
    }
}
