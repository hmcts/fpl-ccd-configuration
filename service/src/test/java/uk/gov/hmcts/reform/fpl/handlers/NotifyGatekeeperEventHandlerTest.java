package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.NotifyGatekeeperTemplate;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
public class NotifyGatekeeperEventHandlerTest {
    private static final String CASE_ID = "12345";

    @Captor
    private ArgumentCaptor<NotifyGatekeeperTemplate> captor;
    @MockBean
    private RequestData requestData;
    @MockBean
    private NotificationService notificationService;
    @Autowired
    private NotifyGatekeeperEventHandler notifyGatekeeperEventHandler;
    @MockBean
    private CaseUrlService caseUrlService;

    @BeforeEach
    void init() {
        when(caseUrlService.getCaseUrl(Long.valueOf(CASE_ID))).thenReturn("http://case/url");
    }

    @Test
    void shouldSendEmailToMultipleGatekeepers() {
        CallbackRequest request = callbackRequest();

        notifyGatekeeperEventHandler.sendEmailToGatekeeper(new NotifyGatekeepersEvent(request, requestData));

        verify(notificationService).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE), eq(GATEKEEPER_EMAIL_ADDRESS),
            captor.capture(), eq(CASE_ID));

        verify(notificationService).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE), eq("Cafcass+gatekeeper@gmail.com"),
            captor.capture(), eq(CASE_ID));

        NotifyGatekeeperTemplate firstTemplate = getExpectedTemplate();
        firstTemplate.setGatekeeperRecipients("Cafcass+gatekeeper@gmail.com has also received this notification");

        NotifyGatekeeperTemplate secondTemplate = getExpectedTemplate();
        secondTemplate.setGatekeeperRecipients(
            "FamilyPublicLaw+gatekeeper@gmail.com has also received this notification");

        assertThat(captor.getAllValues()).usingFieldByFieldElementComparator()
            .containsOnly(firstTemplate, secondTemplate);
    }

    private NotifyGatekeeperTemplate getExpectedTemplate() {
        NotifyGatekeeperTemplate expectedTemplate = new NotifyGatekeeperTemplate();
        expectedTemplate.setCaseUrl("http://case/url");
        expectedTemplate.setDataPresent(YES.getValue());
        expectedTemplate.setFirstRespondentName("Smith");
        expectedTemplate.setFullStop(NO.getValue());
        expectedTemplate.setReference(CASE_ID);
        expectedTemplate.setNonUrgentHearing(NO.getValue());
        expectedTemplate.setTimeFramePresent(YES.getValue());
        expectedTemplate.setTimeFrameValue("same day");
        expectedTemplate.setUrgentHearing(YES.getValue());
        expectedTemplate.setOrdersAndDirections(List.of("Emergency protection order", "Contact with any named person"));
        expectedTemplate.setLocalAuthority("Example Local Authority");
        return expectedTemplate;
    }
}
