package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.controllers.orders.UploadDraftOrdersController;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadDraftOrdersAboutToStartControllerTest extends AbstractUploadDraftOrdersControllerTest {

    @Test
    void shouldReturnDynamicListsAndHearingsWithSealedCMOs() {
        Element<HearingOrder> cmo = element(HearingOrder.builder()
            .order(DocumentReference.builder().build())
            .status(CMOStatus.SEND_TO_JUDGE)
            .build());

        List<Element<HearingBooking>> hearings = List.of(
            hearing(LocalDateTime.of(2020, 3, 3, 11, 30)),
            hearing(LocalDateTime.of(3000, 3, 3, 11, 30)),
            hearingWithCMOId(LocalDateTime.of(2020, 3, 2, 11, 30), cmo.getId())
        );

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .draftUploadedCMOs(List.of(cmo))
            .build();

        CaseData responseData = extractCaseData(postAboutToStartEvent(caseData));

        Pair<String, UUID> noHearingOption = option("No hearing", DynamicListElement.DEFAULT_CODE);
        Pair<String, UUID> hearing1Option = option("Case management hearing, 2 March 2020", hearings.get(2).getId());
        Pair<String, UUID> hearing2Option = option("Case management hearing, 3 March 2020", hearings.get(0).getId());
        Pair<String, UUID> hearing3Option = option("Case management hearing, 3 March 3000", hearings.get(1).getId());

        UploadDraftOrdersData expectedEventData = UploadDraftOrdersData.builder()
            .pastHearingsForCMO(dynamicList(hearing2Option))
            .futureHearingsForCMO(dynamicList(hearing3Option))
            .draftOrderHearings(dynamicList(noHearingOption, hearing1Option, hearing2Option, hearing3Option))
            .showCMOsSentToJudge(YesNo.YES)
            .cmosSentToJudge("Case management hearing, 2 March 2020")
            .build();

        CaseData expectedCaseData = caseData.toBuilder().uploadDraftOrdersEventData(expectedEventData).build();

        assertThat(responseData).isEqualTo(expectedCaseData);
    }

    @SafeVarargs
    private Map<String, Object> dynamicList(Pair<String, UUID>... options) {
        return mapper.convertValue(dynamicListWithoutSelected(options), new TypeReference<>() {
        });
    }
}
