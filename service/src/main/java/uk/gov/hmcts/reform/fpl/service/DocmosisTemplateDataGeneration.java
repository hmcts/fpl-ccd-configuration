package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

public abstract class DocmosisTemplateDataGeneration {

    // REFACTOR: 05/12/2019 make not static when all document generation uses this abstract class
    public static String generateDraftWatermarkEncodedString() throws IOException {
        InputStream is = DocmosisTemplateDataGeneration.class.getResourceAsStream("/assets/images/draft-watermark.png");
        byte[] fileContent = is.readAllBytes();
        return Base64.getEncoder().encodeToString(fileContent);
    }

    protected Map<String, Object> getDraftWaterMarkData() throws IOException {
        return ImmutableMap.of(
            "draftbackground", String.format("image:base64:%1$s", generateDraftWatermarkEncodedString())
        );
    }

    // QUESTION: 05/12/2019 Should there be some sort of contract to check if the template data will be for a draft
    //  document? Maybe a boolean parameter or a method of some sort?
    public abstract Map<String, Object> getTemplateData(CaseData caseData) throws IOException;
}
