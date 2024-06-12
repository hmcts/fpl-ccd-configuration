package uk.gov.hmcts.reform.fpl.service.cmo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersApproved;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRejected;
import uk.gov.hmcts.reform.fpl.events.cmo.ReviewCMOEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class DraftOrdersEventNotificationBuilderTest {
    private final DraftOrdersEventNotificationBuilder underTest = new DraftOrdersEventNotificationBuilder();

    @Test
    void shouldReturnEmptyListWhenNoOrdersToBeSent() {
        CaseData caseData = CaseData.builder().build();

        List<ReviewCMOEvent> events = underTest.buildEventsToPublish(caseData);

        assertThat(events).isEmpty();
    }

    @Test
    void shouldReturnCMOIssuedEventWhenOnlyCMOApproved() {
        HearingOrder approvedCMO = buildOrder(AGREED_CMO, APPROVED);
        List<Element<HearingOrder>> ordersToBeSent = wrapElements(List.of(approvedCMO));

        CaseData caseData = CaseData.builder()
            .ordersToBeSent(ordersToBeSent)
            .build();

        List<ReviewCMOEvent> events = underTest.buildEventsToPublish(caseData);

        assertThat(events).usingRecursiveComparison().isEqualTo(List.of(
            new CaseManagementOrderIssuedEvent(caseData, approvedCMO)));
    }

    @Test
    void shouldReturnCMOIssuedAndDraftOrdersRejectedEventsWhenCMOApprovedAndC21sRejected() {
        HearingOrder approvedCMO = buildOrder(AGREED_CMO, APPROVED);
        HearingOrder rejectedC21 = buildOrder(C21, RETURNED);

        List<Element<HearingOrder>> ordersToBeSent = wrapElements(List.of(approvedCMO, rejectedC21));

        CaseData caseData = CaseData.builder()
            .ordersToBeSent(ordersToBeSent)
            .build();

        List<ReviewCMOEvent> events = underTest.buildEventsToPublish(caseData);

        assertThat(events).usingRecursiveComparison().isEqualTo(List.of(
            new CaseManagementOrderIssuedEvent(caseData, approvedCMO),
            new DraftOrdersRejected(caseData, List.of(rejectedC21))));
    }

    @Test
    void shouldReturnDraftOrdersApprovedEventWhenCMOAndC21sApproved() {
        HearingOrder approvedCMO = buildOrder(AGREED_CMO, APPROVED);
        HearingOrder approvedC21 = buildOrder(C21, APPROVED);

        List<Element<HearingOrder>> ordersToBeSent = wrapElements(List.of(approvedCMO, approvedC21));

        CaseData caseData = CaseData.builder()
            .ordersToBeSent(ordersToBeSent)
            .build();

        List<ReviewCMOEvent> events = underTest.buildEventsToPublish(caseData);

        assertThat(events).usingRecursiveComparison().isEqualTo(List.of(
            new DraftOrdersApproved(caseData, unwrapElements(ordersToBeSent), List.of())));
    }

    @Test
    void shouldReturnCMORejectedEventWhenOnlyCMORejected() {
        HearingOrder rejectedCMO = buildOrder(AGREED_CMO, RETURNED);

        List<Element<HearingOrder>> ordersToBeSent = wrapElements(List.of(rejectedCMO));

        CaseData caseData = CaseData.builder()
            .ordersToBeSent(ordersToBeSent)
            .build();

        List<ReviewCMOEvent> events = underTest.buildEventsToPublish(caseData);

        assertThat(events).usingRecursiveComparison().isEqualTo(List.of(
            new CaseManagementOrderRejectedEvent(caseData, rejectedCMO)));
    }

    @Test
    void shouldReturnCMORejectedAndDraftOrdersApprovedEventsWhenCMORejectedAndC21sApproved() {
        HearingOrder rejectedCMO = buildOrder(AGREED_CMO, RETURNED);
        HearingOrder approvedC21 = buildOrder(C21, APPROVED);

        List<Element<HearingOrder>> ordersToBeSent = wrapElements(List.of(rejectedCMO, approvedC21));

        CaseData caseData = CaseData.builder()
            .ordersToBeSent(ordersToBeSent)
            .build();

        List<ReviewCMOEvent> events = underTest.buildEventsToPublish(caseData);

        assertThat(events).usingRecursiveComparison().isEqualTo(List.of(
            new CaseManagementOrderRejectedEvent(caseData, rejectedCMO),
            new DraftOrdersApproved(caseData, List.of(approvedC21), List.of())));
    }

    @Test
    void shouldReturnDraftOrdersRejectedEventWhenCMOAndC21sRejected() {
        HearingOrder rejectedCMO = buildOrder(AGREED_CMO, RETURNED);
        HearingOrder rejectedC21 = buildOrder(C21, RETURNED);

        List<Element<HearingOrder>> ordersToBeSent = wrapElements(List.of(rejectedCMO, rejectedC21));

        CaseData caseData = CaseData.builder()
            .ordersToBeSent(ordersToBeSent)
            .build();

        List<ReviewCMOEvent> events = underTest.buildEventsToPublish(caseData);

        assertThat(events).usingRecursiveComparison().isEqualTo(List.of(
            new DraftOrdersRejected(caseData, unwrapElements(ordersToBeSent))));
    }

    private HearingOrder buildOrder(HearingOrderType type, CMOStatus status) {
        return HearingOrder.builder()
            .type(type)
            .status(status)
            .build();
    }

}
