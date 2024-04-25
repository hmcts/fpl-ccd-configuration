package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.controllers.orders.UploadDraftOrdersController;
import uk.gov.hmcts.reform.fpl.enums.CMOType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.DraftOrderUrgencyOption;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.CMO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(UploadDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadDraftOrdersAboutToStartControllerTest extends AbstractUploadDraftOrdersControllerTest {

    private static final DocumentReference DOCUMENT_REFERENCE = testDocumentReference();

    UploadDraftOrdersAboutToStartControllerTest() {
        super();
    }

    @Test
    void shouldClearDraftOrderUrgencyProperty() {
        List<Element<HearingBooking>> hearings = hearingsOnDateAndDayAfter(now().plusDays(3));

        UploadDraftOrdersData eventData = UploadDraftOrdersData.builder()
            .hearingOrderDraftKind(List.of(CMO))
            .futureHearingsForCMO(dynamicList(hearings))
            .uploadedCaseManagementOrder(DOCUMENT_REFERENCE)
            .cmoUploadType(CMOType.DRAFT)
            .build();

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(eventData)
            .hearingDetails(hearings)
            .draftOrderUrgency(DraftOrderUrgencyOption.builder().urgency(List.of(YES)).build())
            .build();

        CaseData responseData = extractCaseData(postAboutToStartEvent(caseData));
        assertThat(responseData.getDraftOrderUrgency()).isNull();
    }

    private DynamicList dynamicList(List<Element<HearingBooking>> hearings) {
        return dynamicLists.from(0,
            Pair.of(hearings.get(0).getValue().toLabel(), hearings.get(0).getId()),
            Pair.of(hearings.get(1).getValue().toLabel(), hearings.get(1).getId())
        );
    }

    private List<Element<HearingBooking>> hearingsOnDateAndDayAfter(LocalDateTime startDate) {
        return List.of(
            hearing(startDate),
            hearing(startDate.plusDays(1))
        );
    }
}
