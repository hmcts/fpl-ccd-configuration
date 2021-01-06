package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UploadDraftOrdersDataTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldNotMapNullValues() {
        UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
            .showReplacementCMO(YesNo.YES)
            .build();

        Map<String, Object> mapped = mapper.convertValue(eventData, new TypeReference<>() {
        });

        assertThat(mapped).isEqualTo(Map.of("showReplacementCMO", "YES"));
    }

    @Test
    void shouldNotMapEmptyValues() {
        UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
            .cmoJudgeInfo("")
            .showReplacementCMO(YesNo.YES)
            .build();

        Map<String, Object> mapped = mapper.convertValue(eventData, new TypeReference<>() {
        });

        assertThat(mapped).isEqualTo(Map.of("showReplacementCMO", "YES"));
    }

}
