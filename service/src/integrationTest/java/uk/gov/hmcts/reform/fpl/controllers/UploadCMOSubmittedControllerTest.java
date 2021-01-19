package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.cmo.UploadCMOController;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftCMOUploadedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_DRAFT_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadCMOController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadCMOSubmittedControllerTest extends AbstractUploadCMOControllerTest {

    static final String JUDGE_EMAIL = "judge@hmcts.gov.uk";
    private static final DocumentReference DOCUMENT_REFERENCE = DocumentReference.builder()
        .binaryUrl("FAKE BINARY")
        .url("FAKE URL")
        .filename("FAKE FILE")
        .build();
    private static final String FAMILY_MAN_CASE_NUMBER = "SACCCCCCCC5676576567";
    private static final long CASE_ID = 12345L;
    private static final String ADMIN_EMAIL = "admin@family-court.com";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    @MockBean
    private NotificationClient notificationClient;

    protected UploadCMOSubmittedControllerTest() {
        super("upload-cmo");
    }

    @Test
    void shouldSendNotificationsIfNewAgreedCMOUploaded() {
        CallbackRequest callbackRequest = callbackRequest(SEND_TO_JUDGE);

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                eq(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE),
                eq(ADMIN_EMAIL),
                anyMap(),
                eq(NOTIFICATION_REFERENCE)
            );

            verify(notificationClient).sendEmail(
                eq(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE),
                eq(JUDGE_EMAIL),
                anyMap(),
                eq(NOTIFICATION_REFERENCE)
            );
        });
    }

    @Test
    void shouldSendToJudgeIfDraftCMOUploaded() {
        CallbackRequest callbackRequest = callbackRequest(DRAFT);

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> verify(notificationClient).sendEmail(
            CMO_DRAFT_UPLOADED_NOTIFICATION_TEMPLATE,
            JUDGE_EMAIL,
            draftEmailTemplate(),
            NOTIFICATION_REFERENCE
        ));
    }

    private Map<String, Object> draftEmailTemplate() {
        DraftCMOUploadedTemplate template = DraftCMOUploadedTemplate.builder()
            .subjectLineWithHearingDate(String.format("Davidson, %s, case management hearing, 3 November 2020",
                FAMILY_MAN_CASE_NUMBER))
            .respondentLastName("Davidson")
            .judgeTitle("Her Honour Judge")
            .judgeName("Judy")
            .caseUrl(String.format("http://fake-url/cases/case-details/%s#DraftOrdersTab", CASE_ID))
            .build();
        return mapper.convertValue(template, new TypeReference<>() {
        });
    }

    private CallbackRequest callbackRequest(CMOStatus status) {
        List<Element<HearingBooking>> hearingsBefore = hearings(LocalDateTime.of(2020, 11, 3, 12, 0));
        List<Element<HearingBooking>> hearings = hearings(
            LocalDateTime.of(2020, 11, 3, 12, 0),
            hearingsBefore.get(0).getId(),
            hearingsBefore.get(1).getId()
        );

        CaseData caseDataBefore = CaseData.builder()
            .hearingDetails(hearingsBefore)
            .draftUploadedCMOs(List.of())
            .build();

        Element<CaseManagementOrder> order = element(order(hearings.get(0).getValue(), status));

        hearings.get(0).getValue().setCaseManagementOrderId(order.getId());

        Judge judy = Judge.builder()
            .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
            .judgeLastName("Judy")
            .judgeEmailAddress(JUDGE_EMAIL)
            .build();

        CaseData caseData = CaseData.builder()
            .respondents1(List.of(element(Respondent.builder()
                .party(RespondentParty.builder().lastName("Davidson").build())
                .build())))
            .draftUploadedCMOs(List.of(order))
            .allocatedJudge(judy)
            .hearingDetails(hearings)
            .caseLocalAuthority(DEFAULT_LA)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .build();

        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.setId(CASE_ID);

        return toCallBackRequest(caseDetails, asCaseDetails(caseDataBefore));
    }

    private CaseManagementOrder order(HearingBooking hearing, CMOStatus status) {
        return CaseManagementOrder.builder()
            .status(status)
            .hearing(hearing.toLabel())
            .order(DOCUMENT_REFERENCE)
            .dateSent(dateNow())
            .build();
    }

    private List<Element<HearingBooking>> hearings(LocalDateTime startDate) {
        return List.of(
            hearing(startDate),
            hearing(startDate.plusDays(1))
        );
    }

    private List<Element<HearingBooking>> hearings(LocalDateTime startDate, UUID id1, UUID id2) {
        return List.of(
            hearing(id1, startDate),
            hearing(id2, startDate.plusDays(1))
        );
    }
}
