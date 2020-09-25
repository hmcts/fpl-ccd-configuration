package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.NoticeOfProceedingsIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForNoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfProceedingsEmailContentProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.ALLOCATED_JUDGE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NoticeOfProceedingsIssuedEventHandler.class, LookupTestConfig.class})
class NoticeOfProceedingsIssuedEventHandlerTest {
    @MockBean
    private RequestData requestData;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NoticeOfProceedingsEmailContentProvider noticeOfProceedingsEmailContentProvider;

    @Autowired
    private NoticeOfProceedingsIssuedEventHandler noticeOfProceedingsIssuedEventHandler;

    @Test
    void shouldNotifyAllocatedJudgeOfIssuedNoticeOfProceedingsWhenNotificationEnabled() {
        final AllocatedJudgeTemplateForNoticeOfProceedings expectedParameters
            = getAllocatedJudgeTemplateParameters();

        CaseData caseData = caseData();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(NOTICE_OF_PROCEEDINGS)).willReturn(true);

        given(noticeOfProceedingsEmailContentProvider.buildAllocatedJudgeNotification(caseData))
            .willReturn(expectedParameters);

        noticeOfProceedingsIssuedEventHandler.notifyAllocatedJudgeOfIssuedStandardDirectionsOrder(
            new NoticeOfProceedingsIssuedEvent(caseData));

        verify(notificationService).sendEmail(
            NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE, ALLOCATED_JUDGE_EMAIL_ADDRESS, expectedParameters,
            "12345");
    }

    @Test
    void shouldNotNotifyAllocatedJudgeOfIssuedNoticeOfProceedingsWhenNotificationDisabled() {
        AllocatedJudgeTemplateForNoticeOfProceedings expectedParameters = getAllocatedJudgeTemplateParameters();
        CaseData caseData = caseData();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(NOTICE_OF_PROCEEDINGS)).willReturn(false);

        given(noticeOfProceedingsEmailContentProvider.buildAllocatedJudgeNotification(caseData))
            .willReturn(expectedParameters);

        noticeOfProceedingsIssuedEventHandler.notifyAllocatedJudgeOfIssuedStandardDirectionsOrder(
            new NoticeOfProceedingsIssuedEvent(caseData));

        verify(notificationService, never()).sendEmail(any(), any(String.class), anyMap(), any());
    }

    @Test
    void shouldNotNotifyAllocatedJudgeOfIssuedNoticeOfProceedingsWhenJudgeNotAllocated() {
        AllocatedJudgeTemplateForNoticeOfProceedings expectedParameters = getAllocatedJudgeTemplateParameters();

        CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .noticeOfProceedings(NoticeOfProceedings.builder().build())
            .build();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(NOTICE_OF_PROCEEDINGS)).willReturn(true);

        given(noticeOfProceedingsEmailContentProvider.buildAllocatedJudgeNotification(caseData))
            .willReturn(expectedParameters);

        noticeOfProceedingsIssuedEventHandler.notifyAllocatedJudgeOfIssuedStandardDirectionsOrder(
            new NoticeOfProceedingsIssuedEvent(caseData));

        verify(notificationService, never()).sendEmail(any(), any(String.class), anyMap(), any());
    }

    private AllocatedJudgeTemplateForNoticeOfProceedings getAllocatedJudgeTemplateParameters() {
        AllocatedJudgeTemplateForNoticeOfProceedings allocatedJudgeTemplate
            = new AllocatedJudgeTemplateForNoticeOfProceedings();
        allocatedJudgeTemplate.setCaseUrl("null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345");
        allocatedJudgeTemplate.setFamilyManCaseNumber("6789");
        allocatedJudgeTemplate.setHearingDate("21 October 2020");
        allocatedJudgeTemplate.setJudgeName("Byrne");
        allocatedJudgeTemplate.setJudgeTitle("Her Honour Judge");
        allocatedJudgeTemplate.setRespondentLastName("Moley");

        return allocatedJudgeTemplate;
    }
}
