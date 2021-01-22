package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.fpl.events.cmo.AgreedCMOUploaded;
import uk.gov.hmcts.reform.fpl.handlers.cmo.AgreedCMOUploadedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.AgreedCMOUploadedContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;

import java.time.LocalDateTime;

import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;

@SpringBootTest(classes = {
    AgreedCMOUploadedEventHandler.class,
    AgreedCMOUploadedContentProvider.class,
    NotificationService.class,
    ObjectMapper.class,
    CaseUrlService.class
})

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AgreedCMOUploadedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private final String respondentLastName = "Smith";
    private final String caseUrl = "http://fake-url/cases/case-details/12345#DraftOrdersTab";
    private CaseData caseData;
    private HearingBooking hearing;
    private Judge allocatedJudge;
    private JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private static final String ALLOCATED_JUDGE_EMAIL = "allocatedjudge@hmcts.gov.uk";
    private static final String TEMP_JUDGE_EMAIL = "tempjudge@hmcts.gov.uk";
    private AgreedCMOUploaded agreedCMOUploaded;

    @Autowired
    private AgreedCMOUploadedEventHandler underTest;

    @BeforeAll
    void setup() {
        allocatedJudge = Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Hastings")
            .judgeEmailAddress(ALLOCATED_JUDGE_EMAIL)
            .build();

        judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Dave")
            .judgeEmailAddress(TEMP_JUDGE_EMAIL)
            .build();
    }

    @Test
    void sendNotificationForAdminWhenTemporaryJudgeAssigned() {
        caseData = caseData();

        hearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2021, 1, 1, 12, 0))
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .build();

        agreedCMOUploaded = new AgreedCMOUploaded(caseData, hearing);
        underTest.sendNotificationForAdmin(agreedCMOUploaded);

        assertThat(response())
            .hasSubject("CMO sent for approval, " + respondentLastName)
            .hasBody(emailContent()
                .line(judgeAndLegalAdvisor.getJudgeTitle()
                    .getLabel()
                    + " "
                    + judgeAndLegalAdvisor.getJudgeLastName()
                    + " has been notified to approve the "
                    + "CMO for:")
                .line()
                .callout(String.format("%s, %s",
                    buildSubjectLine(caseData.getFamilyManCaseNumber(), caseData.getAllRespondents()),
                    uncapitalize(hearing.toLabel())))
                .line()
                .line("To view the order, sign in to:")
                .end(caseUrl)
            );
    }

    @Test
    void sendNotificationForAdminWhenNoTemporaryJudgeAssigned() {
        caseData = caseData().toBuilder().allocatedJudge(allocatedJudge).build();

        hearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2021, 1, 1, 12, 0))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
            .build();

        agreedCMOUploaded = new AgreedCMOUploaded(caseData, hearing);
        underTest.sendNotificationForAdmin(agreedCMOUploaded);

        assertThat(response())
            .hasSubject("CMO sent for approval, " + respondentLastName)
            .hasBody(emailContent()
                .line(allocatedJudge.getJudgeTitle().getLabel()
                    + " "
                    + allocatedJudge.getJudgeLastName()
                    + " has been notified to approve the "
                    + "CMO for:")
                .line()
                .callout(String.format("%s, %s",
                    buildSubjectLine(caseData.getFamilyManCaseNumber(), caseData.getAllRespondents()),
                    uncapitalize(hearing.toLabel())))
                .line()
                .line("To view the order, sign in to:")
                .end(caseUrl)
            );
    }

    @Test
    void sendNotificationForJudge() {
        caseData = caseData();

        hearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2021, 1, 1, 12, 0))
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .build();

        agreedCMOUploaded = new AgreedCMOUploaded(caseData, hearing);
        underTest.sendNotificationForJudge(agreedCMOUploaded);

        assertThat(response())
            .hasSubject("CMO ready for approval, " + respondentLastName)
            .hasBody(emailContent()
                .line("Dear " + judgeAndLegalAdvisor.getJudgeTitle()
                    .getLabel() + " " + judgeAndLegalAdvisor.getJudgeLastName() + ",")
                .line()
                .line("The CMO has been received for:")
                .line()
                .callout(String.format("%s, %s",
                    buildSubjectLine(caseData.getFamilyManCaseNumber(), caseData.getAllRespondents()),
                    uncapitalize(hearing.toLabel())))
                .line()
                .line("You should now check the order to either:")
                .list("approve and seal")
                .list("amend, or")
                .list("reject and return")
                .line()
                .line("To view the order, sign in to:")
                .end(caseUrl)
            );
    }

}
