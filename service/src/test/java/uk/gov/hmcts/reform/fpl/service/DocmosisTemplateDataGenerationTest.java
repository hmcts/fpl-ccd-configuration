package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.service.DocmosisTemplateDataGeneration.generateDraftWatermarkEncodedString;

class DocmosisTemplateDataGenerationTest {

    private static final DocmosisTemplateDataGeneration templateDataGeneration = new DocmosisTemplateDataGeneration() {
        @Override
        public Map<String, Object> getTemplateData(CaseData caseData) {
            throw new NotImplementedException("Not testing this method");
        }
    };

    @Test
    void shouldGenerateEncodedStringWhenGenerateDraftWatermarkCalled() throws IOException {
        String encodedImgString = generateDraftWatermarkEncodedString();
        assertThat(encodedImgString).isNotEqualTo("");
    }

    @Test
    void shouldPopulateAMapWithWaterMarkEncoding() throws IOException {
        final Map<String, Object> draftWaterMarkData = templateDataGeneration.getDraftWaterMarkData();
        // Can't think of a nicer way to assert this, can't mock static methods with mockito
        assertThat(draftWaterMarkData).containsKey("draftbackground")
            .extracting("draftbackground")
            .asString()
            .isNotEmpty();
    }
}
