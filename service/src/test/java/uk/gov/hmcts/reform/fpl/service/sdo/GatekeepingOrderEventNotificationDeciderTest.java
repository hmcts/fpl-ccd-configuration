package uk.gov.hmcts.reform.fpl.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.SDO;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.SDO_AND_NOP;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.URGENT_AND_NOP;

class GatekeepingOrderEventNotificationDeciderTest {

    private static final DocumentReference ORDER = mock(DocumentReference.class);

    private final GatekeepingOrderEventNotificationDecider underTest = new GatekeepingOrderEventNotificationDecider();

    @Test
    void buildEventToPublishForUnsealedSDOAndNoHearingOrder() {
        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder().orderStatus(OrderStatus.DRAFT).build())
            .build();

        assertThat(underTest.buildEventToPublish(caseData)).isEmpty();
    }

    @Test
    void buildEventToPublishForUnsealedSDOWhenInCaseManagement() {
        CaseData caseData = CaseData.builder()
            .state(CASE_MANAGEMENT)
            .standardDirectionOrder(StandardDirectionOrder.builder().orderStatus(OrderStatus.DRAFT).build())
            .urgentHearingOrder(mock(UrgentHearingOrder.class))
            .build();

        assertThat(underTest.buildEventToPublish(caseData)).isEmpty();
    }

    @Test
    void buildEventToPublishForUrgentHearingOrder() {
        CaseData caseData = CaseData.builder()
            .state(GATEKEEPING)
            .urgentHearingOrder(UrgentHearingOrder.builder().order(ORDER).build())
            .build();

        assertThat(underTest.buildEventToPublish(caseData)).contains(GatekeepingOrderEvent.builder()
            .notificationGroup(URGENT_AND_NOP)
                .order(ORDER)
                .caseData(caseData)
                .build()
        );
    }

    @Test
    void buildEventToPublishForSDOAndNoPWhenInGateKeeping() {
        CaseData caseData = CaseData.builder()
            .state(GATEKEEPING)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(OrderStatus.SEALED)
                .orderDoc(ORDER)
                .build())
            .build();

        assertThat(underTest.buildEventToPublish(caseData)).contains(GatekeepingOrderEvent.builder()
            .notificationGroup(SDO_AND_NOP)
                .order(ORDER)
                .caseData(caseData)
                .build()
        );
    }

    @Test
    void buildEventToPublishForSDOOnlyWhenInCaseManagement() {
        CaseData caseData = CaseData.builder()
            .state(CASE_MANAGEMENT)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(OrderStatus.SEALED)
                .orderDoc(ORDER)
                .build())
            .build();

        assertThat(underTest.buildEventToPublish(caseData)).contains(GatekeepingOrderEvent.builder()
            .notificationGroup(SDO)
                .order(ORDER)
                .caseData(caseData)
                .build()
        );
    }
}
