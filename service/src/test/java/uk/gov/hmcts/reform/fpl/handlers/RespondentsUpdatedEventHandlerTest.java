package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.cafcass.ChangeOfAddressData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.representative.RegisteredRepresentativeSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.representative.UnregisteredRepresentativeSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.RegisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.UnregisteredRepresentativeSolicitorContentProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.CHANGE_OF_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.assertions.AnnotationAssertion.assertClass;

@ExtendWith(MockitoExtension.class)
class RespondentsUpdatedEventHandlerTest {

    private static final long CASE_ID = 123L;

    @Mock
    private RegisteredRepresentativeSolicitorTemplate registeredTemplate;

    @Mock
    private UnregisteredRepresentativeSolicitorTemplate unregisteredTemplate;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RespondentService respondentService;

    @Mock
    private RegisteredRepresentativeSolicitorContentProvider registeredContentProvider;

    @Mock
    private UnregisteredRepresentativeSolicitorContentProvider unregisteredContentProvider;

    @InjectMocks
    private RespondentsUpdatedEventHandler underTest;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private CafcassNotificationService cafcassNotificationService;

    @Test
    void shouldOnlySendEmailToUpdatedRegisteredSolicitors() {
        final String recipient1 = "solicitor1@test.com";
        final String recipient2 = "solicitor2@test.com";

        final Respondent respondent1 = Respondent.builder()
            .solicitor(RespondentSolicitor.builder().email(recipient1).build())
            .party(RespondentParty.builder().firstName("David").lastName("Jones").build()).build();
        final Respondent respondent2 = Respondent.builder()
            .solicitor(RespondentSolicitor.builder().email(recipient2).build())
            .party(RespondentParty.builder().firstName("Emma").lastName("White").build()).build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(wrapElements(respondent1, respondent2))
            .build();

        final CaseData caseDataBefore = CaseData.builder()
            .respondents1(wrapElements(respondent1))
            .build();

        final RespondentsUpdated respondentsUpdated = new RespondentsUpdated(caseData, caseDataBefore);

        when(respondentService.getRespondentsWithRegisteredSolicitors(caseData.getRespondents1()))
            .thenReturn(new ArrayList<>(List.of(respondent1, respondent2)));

        when(respondentService.getRespondentsWithRegisteredSolicitors(caseDataBefore.getRespondents1()))
            .thenReturn(new ArrayList<>(List.of(respondent1)));

        when(registeredContentProvider.buildContent(caseData, respondent2))
            .thenReturn(registeredTemplate);

        underTest.notifyRegisteredRespondentSolicitors(respondentsUpdated);

        verify(notificationService).sendEmail(
            REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, recipient2, registeredTemplate, CASE_ID
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendEmailToUnregisteredSolicitor() {
        final String expectedEmail = "test@test.com";
        final CaseData caseDataBefore = CaseData.builder().build();

        final Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder().firstName("John").lastName("Smith").build())
            .solicitor(RespondentSolicitor.builder().email(expectedEmail).build())
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(wrapElements(respondent))
            .build();

        final RespondentsUpdated submittedCaseEvent = new RespondentsUpdated(caseData, caseDataBefore);

        when(respondentService.getRespondentsWithUnregisteredSolicitors(caseData.getRespondents1()))
            .thenReturn(new ArrayList<>(List.of(respondent)));

        when(unregisteredContentProvider.buildContent(caseData, respondent))
            .thenReturn(unregisteredTemplate);

        underTest.notifyUnregisteredRespondentSolicitors(submittedCaseEvent);

        verify(notificationService).sendEmail(
            UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, expectedEmail, unregisteredTemplate, CASE_ID
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailWhenNoRespondentsExist() {
        final CaseData caseData = CaseData.builder().respondents1(emptyList()).build();

        final CaseData caseDataBefore = CaseData.builder().build();
        final RespondentsUpdated respondentsUpdated = new RespondentsUpdated(caseData, caseDataBefore);

        underTest.notifyRegisteredRespondentSolicitors(respondentsUpdated);
        underTest.notifyUnregisteredRespondentSolicitors(respondentsUpdated);

        verifyNoInteractions(registeredContentProvider);
    }

    @Test
    void shouldExecuteAsynchronously() {
        assertClass(RespondentsUpdatedEventHandler.class).hasAsyncMethods(
            "notifyChangeOfAddress",
            "notifyRegisteredRespondentSolicitors", "notifyUnregisteredRespondentSolicitors"
        );
    }

    @Test
    void notifyChangeOfAddress() {
        final String recipient1 = "solicitor1@test.com";
        final String recipient2 = "solicitor2@test.com";

        final Respondent respondent = Respondent.builder()
            .solicitor(RespondentSolicitor.builder().email(recipient1).build())
            .party(RespondentParty.builder().firstName("David").lastName("Jones").build()).build();

        List<Element<Respondent>> respondents = wrapElements(respondent);

        CaseData caseData = mock(CaseData.class);
        CaseData caseDataBefore = mock(CaseData.class);

        when(caseData.getCaseLocalAuthority()).thenReturn("SW");
        when(caseData.getAllRespondents()).thenReturn(respondents);
        when(caseDataBefore.getAllRespondents()).thenReturn(respondents);
        when(respondentService.hasAddressChange(respondents, respondents)).thenReturn(true);
        when(cafcassLookupConfiguration.getCafcassEngland("SW"))
            .thenReturn(
                Optional.of(new CafcassLookupConfiguration.Cafcass("SW", ""))
            );
        underTest.notifyChangeOfAddress(new RespondentsUpdated(caseData, caseDataBefore));

        verify(cafcassNotificationService).sendEmail(caseData,
            CHANGE_OF_ADDRESS, ChangeOfAddressData.builder().build());
    }
}
