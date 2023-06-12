package uk.gov.hmcts.reform.fpl.service.sdo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.events.ListAdminEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType.SDO;
import static uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType.UDO;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;

@ExtendWith(MockitoExtension.class)
class ListAdminEventNotificationDeciderTest {

    @InjectMocks
    private ListAdminEventNotificationDecider listAdminEventNotificationDecider;

    @Test
    void shouldReturnListAdminEventForStandardDirectionOrder() {

        final String sendToAdminReason = "Reason text";
        final DocumentReference documentReference = DocumentReference.builder().build();
        final StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderDoc(documentReference)
            .translationRequirements(LanguageTranslationRequirement.NO)
            .dateOfIssue("12 March 2023")
            .build();

        final CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(SERVICE)
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderListOrSendToAdmin("NO")
                .gatekeepingOrderListOrSendToAdminReason(sendToAdminReason)
                .build())
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        final ListAdminEvent expectedEvent = ListAdminEvent.builder()
            .caseData(caseData)
            .isSentToAdmin(true)
            .sendToAdminReason(sendToAdminReason)
            .directionsOrderType(SDO)
            .order(documentReference)
            .build();

        final Optional<ListAdminEvent> listAdminEvent = listAdminEventNotificationDecider.buildEventToPublish(caseData);

        assertThat(listAdminEvent).isPresent();
        assertThat(listAdminEvent.get()).isEqualTo(expectedEvent);
    }

    @Test
    void shouldReturnListAdminEventForUrgentDirectionOrder() {

        final String sendToAdminReason = "Reason text";
        final DocumentReference documentReference = DocumentReference.builder().build();
        final StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderDoc(documentReference)
            .translationRequirements(LanguageTranslationRequirement.NO)
            .dateOfIssue("12 March 2023")
            .build();

        final CaseData caseData = CaseData.builder()
            .urgentDirectionsRouter(SERVICE)
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderListOrSendToAdmin("NO")
                .gatekeepingOrderListOrSendToAdminReason(sendToAdminReason)
                .build())
            .urgentDirectionsOrder(standardDirectionOrder)
            .build();

        final ListAdminEvent expectedEvent = ListAdminEvent.builder()
            .caseData(caseData)
            .isSentToAdmin(true)
            .sendToAdminReason(sendToAdminReason)
            .directionsOrderType(UDO)
            .order(documentReference)
            .build();

        final Optional<ListAdminEvent> listAdminEvent = listAdminEventNotificationDecider.buildEventToPublish(caseData);

        assertThat(listAdminEvent).isPresent();
        assertThat(listAdminEvent.get()).isEqualTo(expectedEvent);
    }

    @Test
    void shouldReturnOptionalEmptyIfGatekeepingOrderListOrSendToAdminIsSetToYes() {

        final String sendToAdminReason = "Reason text";
        final DocumentReference documentReference = DocumentReference.builder().build();
        final StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderDoc(documentReference)
            .translationRequirements(LanguageTranslationRequirement.NO)
            .dateOfIssue("12 March 2023")
            .build();

        final CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(SERVICE)
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderListOrSendToAdmin("YES")
                .gatekeepingOrderListOrSendToAdminReason(sendToAdminReason)
                .build())
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        final Optional<ListAdminEvent> listAdminEvent = listAdminEventNotificationDecider.buildEventToPublish(caseData);

        assertThat(listAdminEvent).isNotPresent();
    }

    @Test
    void shouldReturnOptionalEmptyIfGatekeepingOrderEventDataIsNotSet() {

        final String sendToAdminReason = "Reason text";
        final DocumentReference documentReference = DocumentReference.builder().build();
        final StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderDoc(documentReference)
            .translationRequirements(LanguageTranslationRequirement.NO)
            .dateOfIssue("12 March 2023")
            .build();

        final CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(SERVICE)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        final Optional<ListAdminEvent> listAdminEvent = listAdminEventNotificationDecider.buildEventToPublish(caseData);

        assertThat(listAdminEvent).isNotPresent();
    }

    @Test
    void shouldReturnOptionalEmptyIfStandardDirectionOrderIsNotSet() {

        final String sendToAdminReason = "Reason text";
        final DocumentReference documentReference = DocumentReference.builder().build();
        final StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderDoc(documentReference)
            .translationRequirements(LanguageTranslationRequirement.NO)
            .dateOfIssue("12 March 2023")
            .build();

        final CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(SERVICE)
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderListOrSendToAdmin("YES")
                .gatekeepingOrderListOrSendToAdminReason(sendToAdminReason)
                .build())
            .build();

        final Optional<ListAdminEvent> listAdminEvent = listAdminEventNotificationDecider.buildEventToPublish(caseData);

        assertThat(listAdminEvent).isNotPresent();
    }

    @Test
    void shouldReturnOptionalEmptyIfDocumentReferenceIsNotSet() {

        final String sendToAdminReason = "Reason text";
        final DocumentReference documentReference = DocumentReference.builder().build();
        final StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .translationRequirements(LanguageTranslationRequirement.NO)
            .dateOfIssue("12 March 2023")
            .build();

        final CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(SERVICE)
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderListOrSendToAdmin("YES")
                .gatekeepingOrderListOrSendToAdminReason(sendToAdminReason)
                .build())
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        final Optional<ListAdminEvent> listAdminEvent = listAdminEventNotificationDecider.buildEventToPublish(caseData);

        assertThat(listAdminEvent).isNotPresent();
    }
}