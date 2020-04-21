package uk.gov.hmcts.reform.fpl.service;

import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public abstract class DocmosisTemplateDataGeneration<T> {
    protected static final String BASE_64 = "image:base64:%1$s";
    private static String familyCourtSeal = null;
    private static String draftWatermark = null;

    // REFACTOR: 05/12/2019 make not static when all document generation uses this abstract class
    protected static String generateDraftWatermarkEncodedString() throws IOException {
        if (draftWatermark == null) {
            InputStream is = DocmosisTemplateDataGeneration.class
                .getResourceAsStream("/assets/images/draft-watermark.png");
            byte[] fileContent = is.readAllBytes();
            draftWatermark = Base64.getEncoder().encodeToString(fileContent);
        }
        return draftWatermark;
    }

    protected static String generateCourtSealEncodedString() throws IOException {
        if (familyCourtSeal == null) {
            InputStream is = DocmosisTemplateDataGeneration.class
                .getResourceAsStream("/assets/images/family-court-seal.png");
            byte[] fileContent = is.readAllBytes();
            familyCourtSeal = Base64.getEncoder().encodeToString(fileContent);
        }
        return familyCourtSeal;
    }

    public abstract T getTemplateData(CaseData caseData) throws IOException;
}
