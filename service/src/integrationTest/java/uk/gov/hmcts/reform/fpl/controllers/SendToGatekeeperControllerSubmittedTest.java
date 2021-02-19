package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.events.OutsourcedCaseSentToGatekeepingEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.service.notify.NotificationClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.OUTSOURCED_CASE_SENT_TO_GATEKEEPING_TEMPLATE;

@ActiveProfiles("integration-test")
@WebMvcTest(SendDocumentController.class)
@OverrideAutoConfiguration(enabled = true)
class SendToGatekeeperControllerSubmittedTest extends AbstractControllerTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String NOTIFICATION_REFERENCE = "localhost/12345";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";

    @MockBean
    private NotificationClient notificationClient;

    @SpyBean
    private EventService eventPublisher;

    SendToGatekeeperControllerSubmittedTest() {
        super("send-to-gatekeeper");
    }

    @Test
    void shouldNotifyManagingLAWhenCaseCreatedOnBehalfOfLA() throws Exception {
        CaseData caseData = CaseData.builder()
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationName("Third party").build())
                .build())
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .id(12345L)
            .build();

        postSubmittedEvent(caseData);

        verify(notificationClient).sendEmail(
            eq(OUTSOURCED_CASE_SENT_TO_GATEKEEPING_TEMPLATE), eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            anyMap(), eq(NOTIFICATION_REFERENCE));

        verify(eventPublisher).publishEvent(any(OutsourcedCaseSentToGatekeepingEvent.class));
        verify(eventPublisher).publishEvent(any(NotifyGatekeepersEvent.class));
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    void shouldNotNotifyManagingLAWhenCaseCreatedByLA() {
        postSubmittedEvent(CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .id(12345L)
            .build());

        verifyNoInteractions(notificationClient);
    }
}
