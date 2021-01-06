package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.CMO;

class UploadCMOEventDataTest {

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

    @Test
    void shouldMapPastHearingListIfNotNull() {
        UUID id = UUID.randomUUID();

        UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
            .draftOrderKinds(List.of(CMO))
            .pastHearingsForCMO(DynamicList.builder().value(DynamicListElement.builder().code(id).build()).build())
            .build();

        UUID selected = eventData.getSelectedHearingId(mapper);

        assertThat(selected).isEqualTo(id);
    }

    @Test
    void shouldMapFutureHearingListIfNotNull() {
        UUID id = UUID.randomUUID();

        UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
            .draftOrderKinds(List.of(CMO, C21))
            .futureHearingsForCMO(DynamicList.builder().value(DynamicListElement.builder().code(id).build()).build())
            .build();

        UUID selected = eventData.getSelectedHearingId(mapper);

        assertThat(selected).isEqualTo(id);
    }
}
