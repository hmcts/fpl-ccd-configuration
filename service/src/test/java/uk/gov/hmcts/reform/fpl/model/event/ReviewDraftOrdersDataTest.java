package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewDraftOrdersDataTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldNotMapNullValues() {
        ReviewDraftOrdersData eventData = ReviewDraftOrdersData.builder()
            .draftCMOExists("Y")
            .reviewDecision1(null)
            .build();

        Map<String, Object> mapped = mapper.convertValue(eventData, new TypeReference<>() {
        });

        assertThat(mapped).isEqualTo(Map.of("draftCMOExists", "Y"));
    }

    @Test
    void shouldNotMapEmptyValues() {
        ReviewDraftOrdersData eventData = ReviewDraftOrdersData.builder()
            .draftOrder1Title("")
            .draftCMOExists("Y")
            .build();

        Map<String, Object> mapped = mapper.convertValue(eventData, new TypeReference<>() {
        });

        assertThat(mapped).isEqualTo(Map.of("draftCMOExists", "Y"));
    }

}
