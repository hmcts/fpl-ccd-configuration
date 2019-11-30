package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DocmosisDraftWatermarkGeneratorService.class})
class DocmosisDraftWatermarkGeneratorServiceTest {
    @Autowired
    private DocmosisDraftWatermarkGeneratorService draftWatermarkGeneratorService;

    @Test
    void shouldGenerateEncodedStringWhengenerateDraftWatermarkCalled() {
        String encodedImgString = draftWatermarkGeneratorService.generateDraftWatermarkEncodedString();
        assertThat(encodedImgString).isNotEqualTo("");
    }
}
