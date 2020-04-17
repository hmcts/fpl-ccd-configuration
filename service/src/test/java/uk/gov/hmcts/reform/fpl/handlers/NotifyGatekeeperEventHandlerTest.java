package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
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
        final Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put("orders0", "^Emergency protection order")
            .put("orders1", "")
            .put("orders2", "")
            .put("orders3", "")
            .put("orders4", "")
            .put("directionsAndInterim", "^Information on the whereabouts of the child")
            .put("timeFramePresent", "Yes")
            .put("timeFrameValue", "same day")
            .put("reference", "12345")
            .put("gatekeeper_recipients", "")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        given(gatekeeperEmailContentProvider.buildRecipientsLabel(any(), any())).willReturn("");

        given(gatekeeperEmailContentProvider.buildGatekeeperNotification(callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedParameters);

        notifyGatekeeperEventHandler.sendEmailToGatekeeper(
            new NotifyGatekeepersEvent(callbackRequest(), requestData));

        verify(notificationService, times(1)).sendEmail(
            GATEKEEPER_SUBMISSION_TEMPLATE, GATEKEEPER_EMAIL_ADDRESS,
            expectedParameters, "12345");

        verify(notificationService, times(1)).sendEmail(
            GATEKEEPER_SUBMISSION_TEMPLATE, "Cafcass+gatekeeper@gmail.com",
            expectedParameters, "12345");
    }
}
