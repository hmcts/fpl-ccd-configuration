package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.model.notify.GatekeeperNotificationTemplate;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.GATEKEEPER_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    NotifyGatekeeperEventHandler.class, JacksonAutoConfiguration.class, LookupTestConfig.class,
    GatekeeperEmailContentProvider.class
})
@TestPropertySource(properties = {"ccd.ui.base.url=http://fake-url"})
public class NotifyGatekeeperEventHandlerTest {
    @Captor
    ArgumentCaptor<GatekeeperNotificationTemplate> captor;
    @MockBean
    private RequestData requestData;
    @MockBean
    private NotificationService notificationService;
    @Autowired
    private NotifyGatekeeperEventHandler notifyGatekeeperEventHandler;

    // is this test now doing too much?
    @Test
    void shouldSendEmailToMultipleGatekeepers() {
        CallbackRequest request = callbackRequest();

        notifyGatekeeperEventHandler.sendEmailToGatekeeper(new NotifyGatekeepersEvent(request, requestData));

        verify(notificationService).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE), eq(GATEKEEPER_EMAIL_ADDRESS),
            captor.capture(), eq("12345"));

        verify(notificationService).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE), eq("Cafcass+gatekeeper@gmail.com"),
            captor.capture(), eq("12345"));

        GatekeeperNotificationTemplate firstTemplate = getExpectedTemplate();
        firstTemplate.setGatekeeperRecipients("Cafcass+gatekeeper@gmail.com has also received this notification");

        GatekeeperNotificationTemplate secondTemplate = firstTemplate.duplicate();
        secondTemplate.setGatekeeperRecipients(
            "FamilyPublicLaw+gatekeeper@gmail.com has also received this notification");

        assertThat(captor.getAllValues()).usingFieldByFieldElementComparator()
            .containsOnly(firstTemplate, secondTemplate);
    }

    private GatekeeperNotificationTemplate getExpectedTemplate() {
        GatekeeperNotificationTemplate expectedTemplate = new GatekeeperNotificationTemplate();
        expectedTemplate.setCaseUrl("http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345");
        expectedTemplate.setDataPresent(YES);
        expectedTemplate.setFirstRespondentName("Smith");
        expectedTemplate.setFullStop(NO);
        expectedTemplate.setReference("12345");
        expectedTemplate.setNonUrgentHearing(NO);
        expectedTemplate.setTimeFramePresent(YES);
        expectedTemplate.setUrgentHearing(YES);
        expectedTemplate.setOrdersAndDirections(List.of("Emergency protection order", "Contact with any named person"));
        expectedTemplate.setLocalAuthority("Example Local Authority");
        return expectedTemplate;
    }
}
