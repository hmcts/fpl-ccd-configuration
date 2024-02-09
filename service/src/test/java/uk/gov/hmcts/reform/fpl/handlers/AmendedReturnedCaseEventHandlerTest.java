package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.AmendedReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ReturnedCaseContentProvider;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.utils.assertions.AnnotationAssertion.assertClass;

@ExtendWith(SpringExtension.class)
class AmendedReturnedCaseEventHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReturnedCaseContentProvider returnedCaseContentProvider;

    @Mock
    private CourtService courtService;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private AmendedReturnedCaseEventHandler amendedReturnedCaseEventHandler;

    @Test
    void shouldExecuteAsynchronously() {
        assertClass(AmendedReturnedCaseEventHandler.class).hasAsyncMethods("notifyAdmin", "notifyCafcass");
    }

    @Test
    void shouldSendEmailToCourtAdmin() {
        when(featureToggleService.isWATaskEmailsEnabled()).thenReturn(true);

        final String expectedEmail = "test@test.com";
        final CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .id(RandomUtils.nextLong())
            .build();
        final ReturnedCaseTemplate expectedTemplate = ReturnedCaseTemplate.builder().build();
        final AmendedReturnedCaseEvent amendedReturnedCaseEvent = new AmendedReturnedCaseEvent(caseData);

        when(courtService.getCourtEmail(caseData))
            .thenReturn(expectedEmail);
        when(returnedCaseContentProvider.parametersWithCaseUrl(caseData))
            .thenReturn(expectedTemplate);

        amendedReturnedCaseEventHandler.notifyAdmin(amendedReturnedCaseEvent);

        verify(notificationService).sendEmail(
            AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE,
            expectedEmail,
            expectedTemplate,
            caseData.getId());

        verify(returnedCaseContentProvider).parametersWithCaseUrl(caseData);
        verify(courtService).getCourtEmail(caseData);
    }

    @Test
    void shouldNotSendEmailToCourtAdminIfToggleOff() {
        when(featureToggleService.isWATaskEmailsEnabled()).thenReturn(false);

        final String expectedEmail = "test@test.com";
        final CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .id(RandomUtils.nextLong())
            .build();
        final ReturnedCaseTemplate expectedTemplate = ReturnedCaseTemplate.builder().build();
        final AmendedReturnedCaseEvent amendedReturnedCaseEvent = new AmendedReturnedCaseEvent(caseData);

        amendedReturnedCaseEventHandler.notifyAdmin(amendedReturnedCaseEvent);

        verify(notificationService, never()).sendEmail(
            AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE,
            expectedEmail,
            expectedTemplate,
            caseData.getId());
    }


    @Test
    void shouldSendEmailToCafcass() {
        final String expectedEmail = "test@test.com";
        final String localAuthority = "LA1";
        final CaseData caseData = CaseData.builder()
            .caseLocalAuthority(localAuthority)
            .id(RandomUtils.nextLong())
            .build();
        final ReturnedCaseTemplate expectedTemplate = ReturnedCaseTemplate.builder().build();
        final AmendedReturnedCaseEvent amendedReturnedCaseEvent = new AmendedReturnedCaseEvent(caseData);

        when(cafcassLookupConfiguration.getCafcass(localAuthority))
            .thenReturn(new Cafcass("Swansea", expectedEmail));
        when(returnedCaseContentProvider.parametersWithApplicationLink(caseData))
            .thenReturn(expectedTemplate);

        amendedReturnedCaseEventHandler.notifyCafcass(amendedReturnedCaseEvent);

        verify(notificationService).sendEmail(
            AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE,
            expectedEmail,
            expectedTemplate,
            caseData.getId());

        verify(returnedCaseContentProvider).parametersWithApplicationLink(caseData);
        verify(cafcassLookupConfiguration).getCafcass(localAuthority);
    }

    @AfterEach
    void verifyNoUnwantedInteractions() {
        verifyNoMoreInteractions(notificationService, returnedCaseContentProvider);
    }
}
