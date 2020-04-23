package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.model.notify.GatekeeperNotificationTemplate;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.GATEKEEPER_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NotifyGatekeeperEventHandler.class, JacksonAutoConfiguration.class, LookupTestConfig.class})
public class NotifyGatekeeperEventHandlerTest {
    @MockBean
    private RequestData requestData;

    @MockBean
    private GatekeeperEmailContentProvider gatekeeperEmailContentProvider;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private NotifyGatekeeperEventHandler notifyGatekeeperEventHandler;

    @Test
    void shouldSendEmailToMultipleGatekeepers() {
        GatekeeperNotificationTemplate expectedTemplate = new GatekeeperNotificationTemplate();
        expectedTemplate.setCaseUrl("null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345");
        expectedTemplate.setDataPresent(YES);
        expectedTemplate.setFirstRespondentName("Jim");
        expectedTemplate.setFullStop(NO);
        expectedTemplate.setReference("12345");
        expectedTemplate.setNonUrgentHearing(NO);
        expectedTemplate.setTimeFramePresent(NO);
        expectedTemplate.setUrgentHearing(NO);
        expectedTemplate.setOrdersAndDirections(List.of("Some order", "another order"));
        expectedTemplate.setGatekeeperRecipients("");
        expectedTemplate.setLocalAuthority("Some LA");

        given(gatekeeperEmailContentProvider.buildRecipientsLabel(any(), any())).willReturn("");

        given(gatekeeperEmailContentProvider.buildGatekeeperNotification(callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedTemplate);

        notifyGatekeeperEventHandler.sendEmailToGatekeeper(
            new NotifyGatekeepersEvent(callbackRequest(), requestData));

        verify(notificationService).sendEmail(
            GATEKEEPER_SUBMISSION_TEMPLATE, GATEKEEPER_EMAIL_ADDRESS,
            expectedTemplate, "12345");

        verify(notificationService).sendEmail(
            GATEKEEPER_SUBMISSION_TEMPLATE, "Cafcass+gatekeeper@gmail.com",
            expectedTemplate, "12345");
    }
}
