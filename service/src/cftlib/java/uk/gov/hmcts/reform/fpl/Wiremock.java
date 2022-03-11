package uk.gov.hmcts.reform.fpl;

import javax.annotation.PostConstruct;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.masonm.JwtMatcherExtension;
import com.github.masonm.JwtStubMappingTransformer;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.stereotype.Component;

@Component
public class Wiremock {
    WireMockServer server;
    @PostConstruct
    void init() {
        server = new WireMockServer(options()
            .port(8765)
            .extensions("com.github.masonm.JwtMatcherExtension", "com.github.masonm.JwtStubMappingTransformer")
            .usingFilesUnderClasspath("wiremock"));
        server.start();
    }
}
