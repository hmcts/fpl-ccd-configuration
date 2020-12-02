package uk.gov.hmcts.reform.fpl.handlers.cmo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.events.cmo.AgreedCMOUploaded;
import uk.gov.hmcts.reform.fpl.handlers.HmctsAdminNotificationHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.CMOReadyToSealTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.AgreedCMOUploadedContentProvider;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AgreedCMOUploadedEventHandler.class})
class AgreedCMOUploadedEventHandlerTest {

    private static final String HMCTS_ADMIN_EMAIL = "admin@hmcts.gov.uk";
    private static final String ALLOCATED_JUDGE_EMAIL = "allocatedjudge@hmcts.gov.uk";
    private static final String TEMP_JUDGE_EMAIL = "tempjudge@hmcts.gov.uk";
    private static final String FAKE_URL = "https://fake.com/case/url";

    private static Judge allocatedJudge = Judge.builder()
        .judgeTitle(HIS_HONOUR_JUDGE)
        .judgeLastName("Hastings")
        .judgeEmailAddress(ALLOCATED_JUDGE_EMAIL)
        .build();

    private static JudgeAndLegalAdvisor tempJudge = JudgeAndLegalAdvisor.builder()
        .judgeTitle(HIS_HONOUR_JUDGE)
        .judgeLastName("Dave")
        .judgeEmailAddress(TEMP_JUDGE_EMAIL)
        .build();

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private HmctsAdminNotificationHandler adminNotificationHandler;

    @MockBean
    private AgreedCMOUploadedContentProvider contentProvider;

    @Autowired
    private AgreedCMOUploadedEventHandler eventHandler;

    @BeforeEach
    void setUp() {
        when(adminNotificationHandler.getHmctsAdminEmail(any(CaseData.class))).thenReturn(HMCTS_ADMIN_EMAIL);
    }

    @Test
    void shouldSendNotificationForAdmin() {
        CaseData caseData = caseData();
        HearingBooking hearing = buildHearing(allocatedJudge);
        CMOReadyToSealTemplate template = expectedJudgeTemplate(allocatedJudge.getJudgeName());

        mockContentProvider(
            caseData.getAllRespondents(),
            caseData.getFamilyManCaseNumber(),
            hearing,
            caseData.getAllocatedJudge(),
            template
        );

        AgreedCMOUploaded event = new AgreedCMOUploaded(caseData, hearing);
        eventHandler.sendNotificationForAdmin(event);

        verify(notificationService).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE,
            HMCTS_ADMIN_EMAIL,
            template,
            caseData.getId().toString()
        );
    }

    @Test
    void shouldSendNotificationToTemporaryHearingJudgeWhenTemporaryJudgeHasEmai() {
        CaseData caseData = caseData();
        HearingBooking hearing = buildHearing(tempJudge);
        CMOReadyToSealTemplate template = expectedJudgeTemplate(tempJudge.getJudgeName());

        mockContentProvider(
            caseData.getAllRespondents(),
            caseData.getFamilyManCaseNumber(),
            hearing,
            hearing.getJudgeAndLegalAdvisor(),
            template
        );

        AgreedCMOUploaded event = new AgreedCMOUploaded(caseData, hearing);
        eventHandler.sendNotificationForJudge(event);

        verify(notificationService).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE,
            TEMP_JUDGE_EMAIL,
            template,
            caseData.getId().toString()
        );
    }

    @Test
    void shouldSendNotificationToAllocatedJudgeWhenTemporaryJudgeHasNoEmail() {
        CaseData caseData = caseData();
        HearingBooking hearing = buildHearing(tempJudge.toBuilder().judgeEmailAddress(null).build());
        CMOReadyToSealTemplate template = expectedJudgeTemplate(allocatedJudge.getJudgeName());

        mockContentProvider(
            caseData.getAllRespondents(),
            caseData.getFamilyManCaseNumber(),
            hearing,
            caseData.getAllocatedJudge(),
            template
        );

        AgreedCMOUploaded event = new AgreedCMOUploaded(caseData, hearing);
        eventHandler.sendNotificationForJudge(event);

        verify(notificationService).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE,
            ALLOCATED_JUDGE_EMAIL,
            template,
            caseData.getId().toString()
        );
    }

    @Test
    void shouldNotSendNotificationWhenNeitherJudgeEmailAddressSet() {
        CaseData caseData = caseData().toBuilder().allocatedJudge(
            allocatedJudge.toBuilder().judgeEmailAddress(null).build()).build();
        HearingBooking hearing = buildHearing(JudgeAndLegalAdvisor.builder().judgeEmailAddress(null).build());
        AgreedCMOUploaded event = new AgreedCMOUploaded(caseData, hearing);

        eventHandler.sendNotificationForJudge(event);

        verifyNoInteractions(notificationService);
    }

    private void mockContentProvider(List<Element<Respondent>> respondents, String familyManNumber,
                                     HearingBooking hearing, AbstractJudge judge,
                                     CMOReadyToSealTemplate template) {
        when(contentProvider.buildTemplate(
            eq(hearing),
            eq(12345L),
            eq(judge),
            eq(respondents),
            eq(familyManNumber)
        )).thenReturn(template);
    }

    private CaseData caseData() {
        CaseData.CaseDataBuilder builder = CaseData.builder()
            .id(12345L)
            .familyManCaseNumber("12345")
            .respondents1(wrapElements(
                Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("Duncan")
                        .lastName("Smith")
                        .build())
                    .build()
                )
            )
            .allocatedJudge(allocatedJudge);

        return builder.build();
    }

    private static CMOReadyToSealTemplate expectedJudgeTemplate(String judgeName) {
        return CMOReadyToSealTemplate.builder()
            .respondentLastName("Smith")
            .judgeTitle("His Honour Judge")
            .judgeName(judgeName)
            .caseUrl(FAKE_URL)
            .subjectLineWithHearingDate("Smith, 12345, Case management hearing, 1 February 2020")
            .build();
    }

    private HearingBooking buildHearing(AbstractJudge judgeAndLegalAdvisor) {
        return HearingBooking.builder()
            .type(HearingType.CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2020, 2, 1, 0, 0))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(judgeAndLegalAdvisor.getJudgeTitle())
                .judgeLastName(judgeAndLegalAdvisor.getJudgeLastName())
                .judgeEmailAddress(judgeAndLegalAdvisor.getJudgeEmailAddress())
                .build())
            .build();
    }
}
