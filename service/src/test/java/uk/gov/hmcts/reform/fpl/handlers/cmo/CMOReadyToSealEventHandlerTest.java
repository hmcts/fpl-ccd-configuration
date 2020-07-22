package uk.gov.hmcts.reform.fpl.handlers.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.cmo.CMOReadyToSealEvent;
import uk.gov.hmcts.reform.fpl.handlers.HmctsAdminNotificationHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.CMOReadyToSealTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.CMOReadyToSealContentProvider;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class CMOReadyToSealEventHandlerTest {

    private static final String HMCTS_ADMIN_EMAIL = "admin@hmcts.gov.uk";
    private static final String HMCTS_JUDGE_EMAIL = "judge@hmcts.gov.uk";

    @Mock
    private NotificationService notificationService;

    @Mock
    private HmctsAdminNotificationHandler adminNotificationHandler;

    @Mock
    private CMOReadyToSealContentProvider contentProvider;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private CMOReadyToSealEventHandler eventHandler;

    @Test
    void shouldSendNotificationForAdmin() {
        CaseData caseData = caseData(false);

        when(adminNotificationHandler.getHmctsAdminEmail(any(EventData.class))).thenReturn(HMCTS_ADMIN_EMAIL);
        when(mapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);

        HearingBooking hearing = buildHearing();
        CMOReadyToSealTemplate template = expectedTemplate("Dave");
        when(contentProvider.buildTemplate(
            eq(hearing),
            eq(caseData),
            eq(12345L),
            eq(buildHearing().getJudgeAndLegalAdvisor()))
        ).thenReturn(template);

        CallbackRequest request = callbackRequest();

        CMOReadyToSealEvent event = new CMOReadyToSealEvent(request, hearing);

        eventHandler.sendNotificationForAdmin(event);

        verify(notificationService).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE,
            HMCTS_ADMIN_EMAIL,
            template,
            request.getCaseDetails().getId().toString()
        );
    }

    @Test
    void shouldSendNotificationForJudge() {
        CaseData caseData = caseData(true);
        when(mapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);

        HearingBooking hearing = buildHearing();
        CMOReadyToSealTemplate template = expectedTemplate("Not Dave");
        when(contentProvider.buildTemplate(
            eq(hearing),
            eq(caseData),
            eq(12345L),
            eq(caseData.getAllocatedJudge()))
        ).thenReturn(template);

        CallbackRequest request = callbackRequest();
        CMOReadyToSealEvent event = new CMOReadyToSealEvent(request, hearing);

        eventHandler.sendNotificationForJudge(event);

        verify(notificationService).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE,
            HMCTS_JUDGE_EMAIL,
            template,
            request.getCaseDetails().getId().toString()
        );
    }

    private CallbackRequest callbackRequest() {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(Map.of())
                .id(12345L)
                .build())
            .build();
    }

    private CaseData caseData(boolean withAllocatedJudge) {
        CaseData.CaseDataBuilder builder = CaseData.builder()
            .familyManCaseNumber("12345")
            .respondents1(wrapElements(
                Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("Duncan")
                        .lastName("Smith")
                        .build())
                    .build()
                )
            );

        if (withAllocatedJudge) {
            builder.allocatedJudge(Judge.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Not Dave")
                .judgeEmailAddress(HMCTS_JUDGE_EMAIL)
                .build());
        }

        return builder.build();
    }

    private static CMOReadyToSealTemplate expectedTemplate(String judgeName) {
        return new CMOReadyToSealTemplate()
            .setRespondentLastName("Smith")
            .setJudgeTitle("His Honour Judge")
            .setJudgeName(judgeName)
            .setCaseUrl("https://fake.com/case/url")
            .setSubjectLineWithHearingDate("Smith, 12345 hearing 1 February 2020");
    }

    private HearingBooking buildHearing() {
        return HearingBooking.builder()
            .startDate(LocalDateTime.of(2020, 2, 1, 0, 0))
            .judgeAndLegalAdvisor(
                JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Dave")
                    .build())
            .build();
    }
}
