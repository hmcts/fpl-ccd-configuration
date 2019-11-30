package uk.gov.hmcts.reform.fpl.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Service
@Slf4j
public class DocmosisDraftWatermarkGeneratorService {
    // TODO: 30/11/2019 TestMe!
    String generateDraftWatermark() {
        InputStream is = getClass().getResourceAsStream("/assets/images/draft-watermark.png");
        byte[] fileContent = new byte[0];
        try {
            fileContent = is.readAllBytes();
        } catch (IOException e) {
            log.error("Unable to generate draft water image for template.", e);
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }
}
