package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.ActionType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.NextHearingType.FINAL_HEARING;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
class CaseManagementOrderTest {

    @Test
    void shouldReturnFalseWhenActionTypeEqualsSendToAllParties() {
        assertFalse(order(SEND_TO_ALL_PARTIES).isDraft());
    }

    @EnumSource(value = ActionType.class, names = {"JUDGE_REQUESTED_CHANGE", "SELF_REVIEW"})
    @ParameterizedTest
    void shouldReturnTrueWhenActionTypeEqualsOtherThanSendToAllParties(ActionType type) {
        assertTrue(order(type).isDraft());
    }

    @Test
    void shouldReturnTrueWhenActionTypeIsNull() {
        assertTrue(CaseManagementOrder.builder().build().isDraft());
    }

    private CaseManagementOrder order(ActionType type) {
        return CaseManagementOrder.builder()
            .action(OrderAction.builder()
                .type(type)
                .build())
            .build();
    }

    @Test
    void shouldSetOrderDocReferenceWhenNotNull() {
        CaseManagementOrder order = CaseManagementOrder.builder().build();
        Document document = document();
        order.setOrderDocReferenceFromDocument(document);

        assertThat(order.getOrderDoc()).isEqualTo(DocumentReference.builder()
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .url(document.links.self.href)
            .build());
    }

    @Test
    void shouldNotSetOrderDocReferenceWhenNull() {
        CaseManagementOrder order = CaseManagementOrder.builder().build();
        order.setOrderDocReferenceFromDocument(null);

        assertThat(order).hasAllNullFieldsOrProperties();
    }

    @Test
    void shouldSetActionWithNullDocumentWhenNotNull() {
        CaseManagementOrder order = CaseManagementOrder.builder().build();
        OrderAction action = OrderAction.builder()
            .type(ActionType.SELF_REVIEW)
            .document(buildFromDocument(document()))
            .changeRequestedByJudge("Changes")
            .nextHearingType(FINAL_HEARING)
            .build();

        order.setActionWithNullDocument(action);

        assertThat(order.getAction()).isEqualTo(action.toBuilder().document(null).build());
    }

    @Test
    void shouldNotSetActionWhenDocumentIsNull() {
        CaseManagementOrder order = CaseManagementOrder.builder().build();
        order.setActionWithNullDocument(null);

        assertThat(order).hasAllNullFieldsOrProperties();
    }

    @Test
    void shouldSetNetHearingFromDynamicElementWhenNotNull() {
        CaseManagementOrder order = CaseManagementOrder.builder().build();
        UUID id = UUID.randomUUID();
        String date = "22/01/01";

        HearingDateDynamicElement element = HearingDateDynamicElement.builder()
            .id(id)
            .date(date)
            .build();

        order.setNextHearingFromDynamicElement(element);

        assertThat(order.getNextHearing()).isEqualTo(NextHearing.builder().id(id).date(date).build());
    }

    @Test
    void shouldNotSetNetHearingWhenDynamicElementIsNull() {
        CaseManagementOrder order = CaseManagementOrder.builder().build();
        order.setNextHearingFromDynamicElement(null);

        assertThat(order).hasAllNullFieldsOrProperties();
    }

    @Test
    void shouldGetIssueDateWhenPresent() {
        CaseManagementOrder order = CaseManagementOrder.builder().dateOfIssue("10 May 2099").build();

        assertThat(order.getDateOfIssueAsDate()).isEqualTo(LocalDate.of(2099, 5, 10));
    }

    @Test
    void shouldDefaultIssueDateWhenNull() {
        CaseManagementOrder order = CaseManagementOrder.builder().build();

        assertThat(order.getDateOfIssueAsDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldAddFieldsToMapWhenNotNull() {
        Schedule schedule = Schedule.builder().includeSchedule("Yes").build();
        List<Element<Recital>> recitals = wrapElements(Recital.builder().title("example").build());
        OrderAction action = OrderAction.builder().changeRequestedByJudge("Changes").build();

        CaseManagementOrder order = CaseManagementOrder.builder()
            .schedule(schedule)
            .recitals(recitals)
            .action(action)
            .build();

        assertThat(order.getCCDFields()).containsValues(schedule, recitals, action);
    }

    @Test
    void shouldNotAddFieldsToMapWhenNull() {
        CaseManagementOrder order = CaseManagementOrder.builder().build();

        assertThat(order.getCCDFields()).isEmpty();
    }
}
