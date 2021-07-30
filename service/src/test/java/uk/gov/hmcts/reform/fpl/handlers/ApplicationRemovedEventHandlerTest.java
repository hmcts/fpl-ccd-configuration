package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscTeamLeadLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.AmendedReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.events.ApplicationRemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.notify.ApplicationRemovedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.OrderAmendedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ApplicationRemovedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.ReturnedCaseContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_REMOVED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.utils.assertions.AnnotationAssertion.assertClass;

@ExtendWith(SpringExtension.class)
class ApplicationRemovedEventHandlerTest {

    public static final String TEAM_LEAD_EMAIL = "test@test.com";
    private static final ApplicationRemovedNotifyData NOTIFY_DATA = mock(ApplicationRemovedNotifyData.class);

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplicationRemovedEmailContentProvider applicationRemovedEmailContentProvider;

    @Mock
    private CtscTeamLeadLookupConfiguration ctscTeamLeadLookupConfiguration;

    @InjectMocks
    private ApplicationRemovedEventHandler applicationRemovedEventHandler;

    @Test
    void shouldSendEmailToCTSCTeamLead() {
        final CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .build();

        AdditionalApplicationsBundle bundle = AdditionalApplicationsBundle.builder().build();

        final ApplicationRemovedEvent applicationRemovedEvent = new ApplicationRemovedEvent(caseData, bundle);

        given(ctscTeamLeadLookupConfiguration.getEmail())
            .willReturn(TEAM_LEAD_EMAIL);

        given(applicationRemovedEmailContentProvider.getNotifyData(caseData, bundle))
            .willReturn(NOTIFY_DATA);

        applicationRemovedEventHandler.notifyTeamLead(applicationRemovedEvent);

        verify(notificationService).sendEmail(
            APPLICATION_REMOVED_NOTIFICATION_TEMPLATE,
            TEAM_LEAD_EMAIL,
            NOTIFY_DATA,
            caseData.getId());
    }
}
