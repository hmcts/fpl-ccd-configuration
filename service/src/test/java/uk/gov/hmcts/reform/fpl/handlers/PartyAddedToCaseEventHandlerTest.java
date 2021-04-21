package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseEvent;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.notify.PartyAddedNotifyData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.PARTY_ADDED_TO_CASE_BY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getExpectedDigitalRepresentativesForAddingPartiesToCase;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getExpectedEmailRepresentativesForAddingPartiesToCase;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {PartyAddedToCaseEventHandler.class, LookupTestConfig.class,
    RepresentativeNotificationService.class, RepresentativesInbox.class})
class PartyAddedToCaseEventHandlerTest {

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private RepresentativeService representativeService;

    @MockBean
    private PartyAddedToCaseContentProvider partyAddedToCaseContentProvider;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private PartyAddedToCaseEventHandler underTest;

    CaseData caseData = caseData();
    CaseData caseDataBefore = caseData();

    @BeforeEach
    void init() {
        given(featureToggleService.hasRSOCaseAccess()).willReturn(true);

        given(representativeService.getUpdatedRepresentatives(caseData.getRepresentatives(),
            caseDataBefore.getRepresentatives(), EMAIL))
            .willReturn(getExpectedEmailRepresentativesForAddingPartiesToCase());

        given(representativeService.getUpdatedRepresentatives(caseData.getRepresentatives(),
            caseDataBefore.getRepresentatives(), DIGITAL_SERVICE))
            .willReturn(getExpectedDigitalRepresentativesForAddingPartiesToCase());
    }

    @Test
    void shouldSendEmailToPartiesWhenAddedToCase() {
        final PartyAddedNotifyData expectedEmailParameters = getPartyAddedByEmailNotificationParameters();
        final PartyAddedNotifyData expectedDigitalParameters = getPartyAddedByDigitalServiceNotificationParameters();

        given(partyAddedToCaseContentProvider.getPartyAddedToCaseNotificationParameters(caseData, EMAIL))
            .willReturn(expectedEmailParameters);

        given(partyAddedToCaseContentProvider.getPartyAddedToCaseNotificationParameters(caseData, DIGITAL_SERVICE))
            .willReturn(expectedDigitalParameters);

        underTest.notifyParties(new PartyAddedToCaseEvent(caseData, caseDataBefore));

        verify(notificationService).sendEmail(
            PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE,
            PARTY_ADDED_TO_CASE_BY_EMAIL_ADDRESS,
            expectedEmailParameters,
            caseData.getId());

        verify(notificationService).sendEmail(
            PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE,
            PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_EMAIL,
            expectedDigitalParameters,
            caseData.getId());
    }

    @Test
    void shouldNotSendEmailToPartiesWhichHaveNotBeenUpdated() {
        given(representativeService.getUpdatedRepresentatives(caseData.getRepresentatives(),
            caseDataBefore.getRepresentatives(), DIGITAL_SERVICE)).willReturn(getUpdatedRepresentatives());

        given(representativeService.getUpdatedRepresentatives(caseData.getRepresentatives(),
            caseDataBefore.getRepresentatives(), EMAIL)).willReturn(Collections.emptyList());

        given(partyAddedToCaseContentProvider.getPartyAddedToCaseNotificationParameters(
            caseData, DIGITAL_SERVICE))
            .willReturn(getPartyAddedByDigitalServiceNotificationParameters());

        underTest.notifyParties(new PartyAddedToCaseEvent(caseData, caseDataBefore));

        verify(notificationService, never()).sendEmail(
            eq(PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE),
            eq(PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_EMAIL),
            any(),
            eq(caseData.getId()));

        verify(notificationService, never()).sendEmail(
            eq(PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE),
            eq(PARTY_ADDED_TO_CASE_BY_EMAIL_ADDRESS),
            any(),
            eq(caseData.getId()));

        verify(notificationService).sendEmail(
            PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE,
            "johnmoley@test.com",
            getPartyAddedByDigitalServiceNotificationParameters(),
            caseData.getId());
    }

    private List<Representative> getUpdatedRepresentatives() {
        return List.of(Representative.builder()
            .fullName("John Moley")
            .email("johnmoley@test.com")
            .servingPreferences(DIGITAL_SERVICE)
            .address(Address.builder()
                .addressLine1("A1")
                .postcode("CR0 2GE")
                .build())
            .build());
    }

    private PartyAddedNotifyData getPartyAddedByEmailNotificationParameters() {
        return PartyAddedNotifyData.builder()
            .firstRespondentLastName("Moley")
            .familyManCaseNumber("123")
            .reference("12345")
            .build();
    }

    private PartyAddedNotifyData getPartyAddedByDigitalServiceNotificationParameters() {
        return PartyAddedNotifyData.builder()
            .firstRespondentLastName("Moley")
            .familyManCaseNumber("123")
            .reference("12345")
            .caseUrl("null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();
    }
}
