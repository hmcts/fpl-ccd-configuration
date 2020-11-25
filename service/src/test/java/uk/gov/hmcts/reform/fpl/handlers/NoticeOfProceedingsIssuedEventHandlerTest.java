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
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfProceedingsEmailContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.ALLOCATED_JUDGE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NoticeOfProceedingsIssuedEventHandler.class, LookupTestConfig.class})
class NoticeOfProceedingsIssuedEventHandlerTest {
    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NoticeOfProceedingsEmailContentProvider noticeOfProceedingsEmailContentProvider;

    @Autowired
    private NoticeOfProceedingsIssuedEventHandler noticeOfProceedingsIssuedEventHandler;

    @Test
    void shouldNotifyAllocatedJudgeOfIssuedNoticeOfProceedings() {
        final CaseData caseData = caseData();

        final AllocatedJudgeTemplateForNoticeOfProceedings expectedParameters
            = getAllocatedJudgeTemplateParameters();

        given(noticeOfProceedingsEmailContentProvider.buildAllocatedJudgeNotification(caseData))
            .willReturn(expectedParameters);

        noticeOfProceedingsIssuedEventHandler.notifyAllocatedJudge(
            new NoticeOfProceedingsIssuedEvent(caseData));

        verify(notificationService).sendEmail(
            NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE,
            ALLOCATED_JUDGE_EMAIL_ADDRESS,
            expectedParameters,
            caseData.getId());
    }

    @Test
    void shouldNotNotifyAllocatedJudgeOfIssuedNoticeOfProceedingsWhenJudgeNotAllocated() {
        CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .noticeOfProceedings(NoticeOfProceedings.builder().build())
            .build();

        AllocatedJudgeTemplateForNoticeOfProceedings expectedParameters = getAllocatedJudgeTemplateParameters();

        given(noticeOfProceedingsEmailContentProvider.buildAllocatedJudgeNotification(caseData))
            .willReturn(expectedParameters);

        noticeOfProceedingsIssuedEventHandler.notifyAllocatedJudge(
            new NoticeOfProceedingsIssuedEvent(caseData));

        verifyNoInteractions(notificationService);
    }

    private AllocatedJudgeTemplateForNoticeOfProceedings getAllocatedJudgeTemplateParameters() {
        return AllocatedJudgeTemplateForNoticeOfProceedings.builder()
            .caseUrl("null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .familyManCaseNumber("6789")
            .hearingDate("21 October 2020")
            .judgeName("Byrne")
            .judgeTitle("Her Honour Judge")
            .respondentLastName("Moley")
            .build();
    }
}
