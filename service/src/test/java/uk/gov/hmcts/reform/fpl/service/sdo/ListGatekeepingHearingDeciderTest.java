package uk.gov.hmcts.reform.fpl.service.sdo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.SDO_OR_UDO_AND_NOP;

@ExtendWith(MockitoExtension.class)
class ListGatekeepingHearingDeciderTest {

    @InjectMocks
    private ListGatekeepingHearingDecider listGatekeepingHearingDecider;

    @Test
    void shouldReturnGatekeepingOrderEventForStandardDirectionsOrder() {

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

        final GatekeepingOrderEvent expectedEvent = GatekeepingOrderEvent.builder()
            .caseData(caseData)
            .order(documentReference)
            .languageTranslationRequirement(LanguageTranslationRequirement.NO)
            .notificationGroup(SDO_OR_UDO_AND_NOP)
            .orderTitle("Gatekeeping order - 12 March 2023")
            .directionsOrderType(DirectionsOrderType.SDO)
            .build();

        final Optional<GatekeepingOrderEvent> gatekeepingOrderEvent
            = listGatekeepingHearingDecider.buildEventToPublish(caseData);

        assertThat(gatekeepingOrderEvent).isPresent();
        assertThat(gatekeepingOrderEvent.get()).isEqualTo(expectedEvent);
    }

    @Test
    void shouldReturnGatekeepingOrderEventForUrgentDirectionsOrder() {

        final DocumentReference documentReference = DocumentReference.builder().build();
        final StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderDoc(documentReference)
            .translationRequirements(LanguageTranslationRequirement.NO)
            .dateOfIssue("12 March 2023")
            .build();

        final CaseData caseData = CaseData.builder()
            .urgentDirectionsRouter(SERVICE)
            .urgentDirectionsOrder(standardDirectionOrder)
            .build();

        final GatekeepingOrderEvent expectedEvent = GatekeepingOrderEvent.builder()
            .caseData(caseData)
            .order(documentReference)
            .languageTranslationRequirement(LanguageTranslationRequirement.NO)
            .notificationGroup(SDO_OR_UDO_AND_NOP)
            .orderTitle("Gatekeeping order - 12 March 2023")
            .directionsOrderType(DirectionsOrderType.UDO)
            .build();

        final Optional<GatekeepingOrderEvent> gatekeepingOrderEvent
            = listGatekeepingHearingDecider.buildEventToPublish(caseData);

        assertThat(gatekeepingOrderEvent).isPresent();
        assertThat(gatekeepingOrderEvent.get()).isEqualTo(expectedEvent);
    }

    @Test
    void shouldReturnOptionEmptyIfStandardDirectionOrderIsNotSet() {

        final CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(SERVICE)
            .build();

        final Optional<GatekeepingOrderEvent> gatekeepingOrderEvent
            = listGatekeepingHearingDecider.buildEventToPublish(caseData);

        assertThat(gatekeepingOrderEvent).isNotPresent();
    }

    @Test
    void shouldReturnOptionEmptyIfDocumentReferenceIsNotSet() {

        final StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .translationRequirements(LanguageTranslationRequirement.NO)
            .dateOfIssue("12 March 2023")
            .build();

        final CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(SERVICE)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        final Optional<GatekeepingOrderEvent> gatekeepingOrderEvent
            = listGatekeepingHearingDecider.buildEventToPublish(caseData);

        assertThat(gatekeepingOrderEvent).isNotPresent();
    }
}