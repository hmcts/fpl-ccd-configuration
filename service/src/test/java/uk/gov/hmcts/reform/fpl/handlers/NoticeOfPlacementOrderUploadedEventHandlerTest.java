package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.NoticeOfPlacementOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.DOCUMENT_CONTENTS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getExpectedEmailRepresentativesForAddingPartiesToCase;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.assertEquals;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForAdminWhenNoRepresentativesServedByPost;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NoticeOfPlacementOrderUploadedEventHandler.class, InboxLookupService.class,
    JacksonAutoConfiguration.class, LookupTestConfig.class, HmctsEmailContentProvider.class,
    RepresentativeNotificationService.class, RepresentativeNotificationHandler.class,
    IssuedOrderAdminNotificationHandler.class, HmctsAdminNotificationHandler.class})
public class NoticeOfPlacementOrderUploadedEventHandlerTest {
    @Captor
    private ArgumentCaptor<Map<String, Object>> dataCaptor;

    @MockBean
    private RequestData requestData;

    @MockBean
    private RepresentativeService representativeService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    @MockBean
    private LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoticeOfPlacementOrderUploadedEventHandler noticeOfPlacementOrderUploadedEventHandler;

    @Test
    void shouldSendEmailForPlacementOrderUploaded() {
        Map<String, Object> parameters = Map.of("respondentLastName", "Nelson",
            "caseUrl", String.format("%s/case/%s/%s/%s", "http://fake-url", JURISDICTION, CASE_TYPE, 1L));
        CaseDetails caseDetails = callbackRequest().getCaseDetails();
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        given(localAuthorityEmailContentProvider.buildNoticeOfPlacementOrderUploadedNotification(
            caseDetails)).willReturn(parameters);

        given(orderIssuedEmailContentProvider.buildNotificationParametersForHmctsAdmin(
            caseDetails, LOCAL_AUTHORITY_CODE, DOCUMENT_CONTENTS, NOTICE_OF_PLACEMENT_ORDER))
            .willReturn(getExpectedParametersForAdminWhenNoRepresentativesServedByPost(false));

        given(orderIssuedEmailContentProvider.buildNotificationParametersForRepresentatives(
            callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, DOCUMENT_CONTENTS, NOTICE_OF_PLACEMENT_ORDER))
            .willReturn(getExpectedParametersForRepresentatives(NOTICE_OF_PLACEMENT_ORDER.getLabel(), false));

        given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), EMAIL))
            .willReturn(getExpectedEmailRepresentativesForAddingPartiesToCase());

        noticeOfPlacementOrderUploadedEventHandler.sendEmailForNoticeOfPlacementOrderUploaded(
            new NoticeOfPlacementOrderUploadedEvent(callbackRequest(), requestData, DOCUMENT_CONTENTS));

        verify(notificationService).sendEmail(
            NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE,
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            parameters,
            "12345");

        verify(notificationService).sendEmail(
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
            COURT_EMAIL_ADDRESS,
            getExpectedParametersForAdminWhenNoRepresentativesServedByPost(false),
            "12345");

        verify(notificationService).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES),
            eq("barney@rubble.com"),
            dataCaptor.capture(),
            eq("12345"));

        assertEquals(dataCaptor.getValue(),
            getExpectedParametersForRepresentatives(NOTICE_OF_PLACEMENT_ORDER.getLabel(), false));
    }
}
