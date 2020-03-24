package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProviderSDOIssued;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;

import java.io.IOException;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.USER_ID;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, LookupTestConfig.class})
public class StandardDirectionsOrderIssuedEventHandlerTest {
    private static final Logger LOG = LoggerFactory.getLogger(StandardDirectionsOrderIssuedEventHandlerTest.class);

    private static CallbackRequest callbackRequest;

    @Mock
    private CafcassEmailContentProviderSDOIssued cafcassEmailContentProviderSDOIssued;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private NotificationService notificationService;

    @Mock
    private LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;

    @Mock
    private InboxLookupService inboxLookupService;

    @InjectMocks
    private StandardDirectionsOrderIssuedEventHandler standardDirectionsOrderIssuedEventHandler;

    static {
        try {
            callbackRequest = callbackRequest();
        } catch (IOException e) {
            LOG.error("An error occurred making CallbackRequest", e);
        }
    }

    @Test
    void shouldNotifyCafcassOfIssuedStandardDirectionsOrder() {
        final Map<String, Object> expectedParameters = getStandardDirectionTemplateParameters();

        given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new CafcassLookupConfiguration.Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

        given(cafcassEmailContentProviderSDOIssued.buildCafcassStandardDirectionOrderIssuedNotification(
            callbackRequest.getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedParameters);

        standardDirectionsOrderIssuedEventHandler.notifyCafcassOfIssuedStandardDirectionsOrder(
            new StandardDirectionsOrderIssuedEvent(callbackRequest, AUTH_TOKEN, USER_ID));

        verify(notificationService).sendEmail(
            STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE,
            CAFCASS_EMAIL_ADDRESS,
            expectedParameters,
            "12345");
    }

    @Test
    void shouldNotifyLocalAuthorityOfIssuedStandardDirectionsOrder() {
        final Map<String, Object> expectedParameters = getStandardDirectionTemplateParameters();

        given(localAuthorityEmailContentProvider.buildLocalAuthorityStandardDirectionOrderIssuedNotification(
            callbackRequest.getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedParameters);

        given(
            inboxLookupService.getNotificationRecipientEmail(callbackRequest.getCaseDetails(), LOCAL_AUTHORITY_CODE))
            .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

        standardDirectionsOrderIssuedEventHandler.notifyLocalAuthorityOfIssuedStandardDirectionsOrder(
            new StandardDirectionsOrderIssuedEvent(callbackRequest, AUTH_TOKEN, USER_ID));

        verify(notificationService).sendEmail(
            STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE, LOCAL_AUTHORITY_EMAIL_ADDRESS, expectedParameters,
            "12345");
    }

    private Map<String, Object> getStandardDirectionTemplateParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", "6789")
            .put("leadRespondentsName", "Moley")
            .put("hearingDate", "21 October 2020")
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();
    }
}
