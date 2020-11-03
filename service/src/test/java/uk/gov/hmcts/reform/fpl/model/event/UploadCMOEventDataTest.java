package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UploadCMOEventDataTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldNotMapNullValues() {
        UploadCMOEventData eventData = UploadCMOEventData.builder().showReplacementCMO(YesNo.YES).build();

        Map<String, Object> mapped = mapper.convertValue(eventData, new TypeReference<>() {});

        assertThat(mapped).isEqualTo(Map.of("showReplacementCMO", "YES"));
    }

    @Test
    void shouldNotMapEmptyValues() {
        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .cmoJudgeInfo("")
            .showReplacementCMO(YesNo.YES)
            .build();

        Map<String, Object> mapped = mapper.convertValue(eventData, new TypeReference<>() {});

        assertThat(mapped).isEqualTo(Map.of("showReplacementCMO", "YES"));
    }

    @Test
    void shouldMapPastHearingListIfNotNull() {
        UUID id = UUID.randomUUID();

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .pastHearingsForCMO(DynamicList.builder().value(DynamicListElement.builder().code(id).build()).build())
            .build();

        UUID selected = eventData.getSelectedHearingId(mapper);

        assertThat(selected).isEqualTo(id);
    }

    @Test
    void shouldMapFutureHearingListIfNotNull() {
        UUID id = UUID.randomUUID();

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .futureHearingsForCMO(DynamicList.builder().value(DynamicListElement.builder().code(id).build()).build())
            .build();

        UUID selected = eventData.getSelectedHearingId(mapper);

        assertThat(selected).isEqualTo(id);
    }
}
