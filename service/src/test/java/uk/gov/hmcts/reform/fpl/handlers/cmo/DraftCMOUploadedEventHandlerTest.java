package uk.gov.hmcts.reform.fpl.handlers.cmo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftCMOUploaded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftCMOUploadedTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.DraftCMOUploadedContentProvider;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_DRAFT_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class DraftCMOUploadedEventHandlerTest {

    private static final String FAMILY_MAN_CASE_NUMBER = "12345";
    private static final Long CASE_ID = 12345L;
    private static final String HEARING_JUDGE_EMAIL = "judge@mailnasia.com";
    private static final String ALLOCATED_JUDGE_EMAIL = "allocated-judge@mailnasia.com";

    @Mock
    private NotificationService notificationService;
    @Mock
    private DraftCMOUploadedContentProvider contentProvider;

    private DraftCMOUploadedEventHandler eventHandler;

    @BeforeEach
    void construct() {
        eventHandler = new DraftCMOUploadedEventHandler(notificationService, contentProvider);
    }

    @Test
    void shouldSendNotificationToJudgeOnHearingIfEmailPresent() {
        HearingBooking hearing = hearingBookingWithJudgeEmail(HEARING_JUDGE_EMAIL);
        CaseData caseData = caseData();
        DraftCMOUploadedTemplate template = template("Matthews", "Her Honour Judge");

        when(contentProvider.buildTemplate(
            hearing, CASE_ID, hearingJudge(HEARING_JUDGE_EMAIL), caseData.getAllRespondents(), FAMILY_MAN_CASE_NUMBER)
        ).thenReturn(template);

        DraftCMOUploaded event = new DraftCMOUploaded(caseData, hearing);

        eventHandler.sendNotificationForJudge(event);

        verify(notificationService).sendEmail(
            CMO_DRAFT_UPLOADED_NOTIFICATION_TEMPLATE,
            HEARING_JUDGE_EMAIL,
            template,
            CASE_ID.toString()
        );

        verify(notificationService, never()).sendEmail(
            any(),
            eq(ALLOCATED_JUDGE_EMAIL),
            any(NotifyData.class),
            any()
        );
    }

    @Test
    void shouldSendNotificationToAllocatedJudgeIfEmailPresent() {
        HearingBooking hearing = hearingBooking();
        CaseData caseData = caseData();
        DraftCMOUploadedTemplate template = template("Dredd", "His Honour Judge");

        when(contentProvider.buildTemplate(
            hearing, CASE_ID, allocatedJudge(), caseData.getAllRespondents(), FAMILY_MAN_CASE_NUMBER)
        ).thenReturn(template);

        DraftCMOUploaded event = new DraftCMOUploaded(caseData, hearing);

        eventHandler.sendNotificationForJudge(event);

        verify(notificationService).sendEmail(
            CMO_DRAFT_UPLOADED_NOTIFICATION_TEMPLATE,
            ALLOCATED_JUDGE_EMAIL,
            template,
            CASE_ID.toString()
        );
    }

    private HearingBooking hearingBooking() {
        return hearingBookingWithJudgeEmail(null);
    }

    private HearingBooking hearingBookingWithJudgeEmail(String email) {
        return HearingBooking.builder()
            .type(HearingType.CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2020, 2, 1, 0, 0))
            .judgeAndLegalAdvisor(hearingJudge(email))
            .build();
    }

    private JudgeAndLegalAdvisor hearingJudge(String email) {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Matthews")
            .judgeEmailAddress(email)
            .build();
    }

    private CaseData caseData() {
        CaseData.CaseDataBuilder builder = CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .respondents1(wrapElements(
                Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("Duncan")
                        .lastName("Smith")
                        .build())
                    .build()
                )
            )
            .allocatedJudge(allocatedJudge());

        return builder.build();
    }

    private Judge allocatedJudge() {
        return Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Dredd")
            .judgeEmailAddress(ALLOCATED_JUDGE_EMAIL)
            .build();
    }

    private DraftCMOUploadedTemplate template(String name, String title) {
        return new DraftCMOUploadedTemplate()
            .setCaseUrl("https://fake.url")
            .setJudgeName(name)
            .setJudgeTitle(title)
            .setRespondentLastName("Smith")
            .setSubjectLineWithHearingDate("Smith, 12345, Case management hearing, 1 February 2020");
    }
}
