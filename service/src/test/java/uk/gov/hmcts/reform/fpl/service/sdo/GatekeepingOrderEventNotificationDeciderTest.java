package uk.gov.hmcts.reform.fpl.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.SDO;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.SDO_AND_NOP;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.URGENT_AND_NOP;

class GatekeepingOrderEventNotificationDeciderTest {

    private static final DocumentReference ORDER = mock(DocumentReference.class);
    private static final LocalDate DATE_ADDED = LocalDate.of(2018, 2, 4);

    private final GatekeepingOrderEventNotificationDecider underTest = new GatekeepingOrderEventNotificationDecider();

    @Test
    void buildEventToPublishForUnsealedSDOAndNoHearingOrderInGatekeepingState() {
        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder().orderStatus(OrderStatus.DRAFT).build())
            .build();

        assertThat(underTest.buildEventToPublish(caseData, GATEKEEPING)).isEmpty();
    }

    @Test
    void buildEventToPublishForUnsealedSDOWhenInCaseManagement() {
        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder().orderStatus(OrderStatus.DRAFT).build())
            .urgentHearingOrder(mock(UrgentHearingOrder.class))
            .build();

        assertThat(underTest.buildEventToPublish(caseData, CASE_MANAGEMENT)).isEmpty();
    }

    @Test
    void buildEventToPublishForUrgentHearingOrder() {
        CaseData caseData = CaseData.builder()
            .urgentHearingOrder(UrgentHearingOrder.builder().order(ORDER).dateAdded(DATE_ADDED)
                .translationRequirements(WELSH_TO_ENGLISH).build())
            .build();

        assertThat(underTest.buildEventToPublish(caseData, GATEKEEPING)).contains(GatekeepingOrderEvent.builder()
            .notificationGroup(URGENT_AND_NOP)
            .order(ORDER)
            .orderTitle("Urgent hearing order - 4 February 2018")
            .languageTranslationRequirement(WELSH_TO_ENGLISH)
            .caseData(caseData)
            .build()
        );
    }

    @Test
    void buildEventToPublishForUrgentHearingOrderIfLanguageRequirementNotPresent() {
        CaseData caseData = CaseData.builder()
            .urgentHearingOrder(UrgentHearingOrder.builder().order(ORDER).dateAdded(DATE_ADDED).build())
            .build();

        assertThat(underTest.buildEventToPublish(caseData, GATEKEEPING)).contains(GatekeepingOrderEvent.builder()
            .notificationGroup(URGENT_AND_NOP)
            .order(ORDER)
            .orderTitle("Urgent hearing order - 4 February 2018")
            .languageTranslationRequirement(NO)
            .caseData(caseData)
            .build()
        );
    }

    @Test
    void buildEventToPublishForSDOAndNoPWhenInGateKeeping() {
        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(OrderStatus.SEALED)
                .orderDoc(ORDER)
                .dateOfIssue("6 August 2020")
                .translationRequirements(WELSH_TO_ENGLISH)
                .build())
            .build();

        assertThat(underTest.buildEventToPublish(caseData, GATEKEEPING)).contains(GatekeepingOrderEvent.builder()
            .notificationGroup(SDO_AND_NOP)
            .order(ORDER)
            .orderTitle("Gatekeeping order - 6 August 2020")
            .languageTranslationRequirement(WELSH_TO_ENGLISH)
            .caseData(caseData)
            .build()
        );
    }

    @Test
    void buildEventToPublishForSDOOnlyWhenInCaseManagement() {
        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(OrderStatus.SEALED)
                .orderDoc(ORDER)
                .dateOfIssue("6 August 2020")
                .translationRequirements(WELSH_TO_ENGLISH)
                .build())
            .build();

        assertThat(underTest.buildEventToPublish(caseData, CASE_MANAGEMENT)).contains(GatekeepingOrderEvent.builder()
            .notificationGroup(SDO)
            .order(ORDER)
            .languageTranslationRequirement(WELSH_TO_ENGLISH)
            .orderTitle("Gatekeeping order - 6 August 2020")
            .caseData(caseData)
            .build()
        );
    }
}
