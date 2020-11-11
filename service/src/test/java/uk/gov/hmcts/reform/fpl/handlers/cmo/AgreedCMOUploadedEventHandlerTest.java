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
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AgreedCMOUploadedEventHandler.class})
class AgreedCMOUploadedEventHandlerTest {

    private static final String HMCTS_ADMIN_EMAIL = "admin@hmcts.gov.uk";
    private static final String HMCTS_JUDGE_EMAIL = "judge@hmcts.gov.uk";
    private static final String FAKE_URL = "https://fake.com/case/url";

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
        CaseData caseData = caseDataWithAllocatedJudgeEmail(HMCTS_JUDGE_EMAIL);
        HearingBooking hearing = buildHearing();
        CMOReadyToSealTemplate template = expectedTemplate();

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
    void shouldSendNotificationForJudge() {
        CaseData caseData = caseDataWithAllocatedJudgeEmail(HMCTS_JUDGE_EMAIL);
        HearingBooking hearing = buildHearing();
        CMOReadyToSealTemplate template = expectedTemplate();

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
            HMCTS_JUDGE_EMAIL,
            template,
            caseData.getId().toString()
        );
    }

    @Test
    void shouldNotSendNotificationForJudgeWhenNoEmailIsProvided() {
        CaseData caseData = caseDataWithoutAllocatedJudgeEmail();
        HearingBooking hearing = buildHearing();
        AgreedCMOUploaded event = new AgreedCMOUploaded(caseData, hearing);

        eventHandler.sendNotificationForJudge(event);

        verify(notificationService, never()).sendEmail(
            anyString(),
            anyString(),
            anyMap(),
            anyString()
        );
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

    private CaseData caseDataWithoutAllocatedJudgeEmail() {
        return caseDataWithAllocatedJudgeEmail("");
    }

    private CaseData caseDataWithAllocatedJudgeEmail(String email) {
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
            .allocatedJudge(Judge.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Hastings")
                .judgeEmailAddress(email)
                .build());

        return builder.build();
    }

    private static CMOReadyToSealTemplate expectedTemplate() {
        return new CMOReadyToSealTemplate()
            .setRespondentLastName("Smith")
            .setJudgeTitle("His Honour Judge")
            .setJudgeName("Hastings")
            .setCaseUrl(FAKE_URL)
            .setSubjectLineWithHearingDate("Smith, 12345, Case management hearing, 1 February 2020");
    }

    private HearingBooking buildHearing() {
        return HearingBooking.builder()
            .type(HearingType.CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2020, 2, 1, 0, 0))
            .judgeAndLegalAdvisor(
                JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Dave")
                    .judgeEmailAddress(HMCTS_JUDGE_EMAIL)
                    .build())
            .build();
    }
}
