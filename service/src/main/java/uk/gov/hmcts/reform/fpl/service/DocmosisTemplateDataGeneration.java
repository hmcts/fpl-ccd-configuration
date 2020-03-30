package uk.gov.hmcts.reform.fpl.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public abstract class DocmosisTemplateDataGeneration {
    static final String BASE_64 = "image:base64:%1$s";

    // REFACTOR: 05/12/2019 make not static when all document generation uses this abstract class
    static String generateDraftWatermarkEncodedString() throws IOException {
        InputStream is = DocmosisTemplateDataGeneration.class.getResourceAsStream("/assets/images/draft-watermark.png");
        byte[] fileContent = is.readAllBytes();
        return Base64.getEncoder().encodeToString(fileContent);
    }

    static String generateCourtSealEncodedString() throws IOException {
        InputStream is = DocmosisTemplateDataGeneration.class
            .getResourceAsStream("/assets/images/family-court-seal.png");
        byte[] fileContent = is.readAllBytes();
        return Base64.getEncoder().encodeToString(fileContent);
    }
}
