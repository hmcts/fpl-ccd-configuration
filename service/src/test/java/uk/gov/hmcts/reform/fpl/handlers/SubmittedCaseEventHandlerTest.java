package uk.gov.hmcts.reform.fpl.handlers;

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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.appendSendToCtscOnCallback;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SubmittedCaseEventHandler.class, JacksonAutoConfiguration.class, LookupTestConfig.class,
    HmctsAdminNotificationHandler.class})
public class SubmittedCaseEventHandlerTest {
    @MockBean
    private RequestData requestData;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private HmctsEmailContentProvider hmctsEmailContentProvider;

    @MockBean
    private CafcassEmailContentProvider cafcassEmailContentProvider;

    @Autowired
    private SubmittedCaseEventHandler submittedCaseEventHandler;

    @Captor
    private ArgumentCaptor<NotifyData> captor;

    @Test
    void shouldSendEmailToHmctsAdminWhenCtscIsDisabled() {
        final SubmitCaseHmctsTemplate expectedTemplate = commonExpectedTemplate(new SubmitCaseHmctsTemplate());
        expectedTemplate.setCourt(COURT_NAME);
        expectedTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);

        given(hmctsEmailContentProvider.buildHmctsSubmissionNotification(callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedTemplate);

        submittedCaseEventHandler.sendEmailToHmctsAdmin(
            new SubmittedCaseEvent(callbackRequest(), requestData));

        verify(notificationService).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE),
            eq(COURT_EMAIL_ADDRESS),
            captor.capture(),
            eq("12345"));

        assertThat(captor.getValue()).isEqualToComparingFieldByField(expectedTemplate);
    }

    @Test
    void shouldSendEmailToCtscAdminWhenCtscIsEnabled() {
        CallbackRequest callbackRequest = appendSendToCtscOnCallback();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        final SubmitCaseHmctsTemplate expectedTemplate = commonExpectedTemplate(new SubmitCaseHmctsTemplate());
        expectedTemplate.setCourt(COURT_NAME);
        expectedTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);

        given(hmctsEmailContentProvider.buildHmctsSubmissionNotification(caseDetails, LOCAL_AUTHORITY_CODE))
            .willReturn(expectedTemplate);

        submittedCaseEventHandler.sendEmailToHmctsAdmin(new SubmittedCaseEvent(callbackRequest, requestData));

        verify(notificationService).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE),
            eq(CTSC_INBOX),
            captor.capture(),
            eq("12345"));

        assertThat(captor.getValue()).isEqualToComparingFieldByField(expectedTemplate);
    }

    @Test
    void shouldSendEmailToCafcass() {
        final SubmitCaseCafcassTemplate expectedTemplate = commonExpectedTemplate(new SubmitCaseCafcassTemplate());
        expectedTemplate.setCafcass(CAFCASS_NAME);
        expectedTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);

        given(cafcassEmailContentProvider.buildCafcassSubmissionNotification(callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedTemplate);

        submittedCaseEventHandler.sendEmailToCafcass(
            new SubmittedCaseEvent(callbackRequest(), requestData));

        verify(notificationService).sendEmail(
            eq(CAFCASS_SUBMISSION_TEMPLATE),
            eq(CAFCASS_EMAIL_ADDRESS),
            captor.capture(),
            eq("12345"));

        assertThat(captor.getValue()).isEqualToComparingFieldByField(expectedTemplate);
    }

    private <T extends SharedNotifyTemplate> T commonExpectedTemplate(T template) {
        template.setCaseUrl("null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345");
        template.setDataPresent(YES.getValue());
        template.setFirstRespondentName("Jim");
        template.setFullStop(NO.getValue());
        template.setReference("12345");
        template.setNonUrgentHearing(NO.getValue());
        template.setTimeFramePresent(NO.getValue());
        template.setUrgentHearing(NO.getValue());
        template.setOrdersAndDirections(List.of("Some order", "another order"));

        return template;
    }
}
