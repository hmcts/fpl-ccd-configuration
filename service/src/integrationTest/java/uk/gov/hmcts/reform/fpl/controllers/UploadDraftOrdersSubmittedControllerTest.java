package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.orders.UploadDraftOrdersController;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftCMOUploadedTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftOrdersUploadedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_DRAFT_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadDraftOrdersSubmittedControllerTest extends AbstractUploadDraftOrdersControllerTest {

    private static final long CASE_ID = 12345L;
    private static final String FAMILY_MAN_CASE_NUMBER = "SACCCCCCCC5676576567";
    private static final String ADMIN_EMAIL = "admin@family-court.com";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;
    private static final LocalDateTime HEARING_DATE = LocalDateTime.of(2020, Month.NOVEMBER, 3, 0, 0, 0);

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @BeforeEach
    void setUp() {
        when(featureToggleService.isSummaryTabOnEventEnabled()).thenReturn(true);
    }

    @Test
    void shouldSendNotificationsIfNewAgreedCMOUploaded() {
        when(featureToggleService.isDraftOrdersEnabled()).thenReturn(false);

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

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID),
            eq("internal-update-case-summary"), anyMap());
    }

    @Test
    void shouldSendNotificationsIfNewAgreedCMOUploadedToggledOff() {
        when(featureToggleService.isSummaryTabOnEventEnabled()).thenReturn(false);

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

        verifyNoInteractions(coreCaseDataService);
    }

    @Test
    void shouldSendToJudgeIfDraftCMOUploaded() {
        when(featureToggleService.isDraftOrdersEnabled()).thenReturn(false);

        CallbackRequest callbackRequest = callbackRequest(DRAFT);

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> verify(notificationClient).sendEmail(
            CMO_DRAFT_UPLOADED_NOTIFICATION_TEMPLATE,
            JUDGE_EMAIL,
            draftEmailTemplate(),
            NOTIFICATION_REFERENCE
        ));

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID),
            eq("internal-update-case-summary"), anyMap());
    }

    private Map<String, Object> draftEmailTemplate() {
        DraftCMOUploadedTemplate template = DraftCMOUploadedTemplate.builder()
            .subjectLineWithHearingDate(String.format("Davidson, %s, case management hearing, 3 November 2020",
                FAMILY_MAN_CASE_NUMBER))
            .respondentLastName("Davidson")
            .judgeTitle("Her Honour Judge")
            .judgeName("Judy")
            .caseUrl("http://fake-url/cases/case-details/12345#Draft%20orders")
            .build();
        return convert(template);
    }

    private Map<String, Object> draftOrdersUploadedEmailCustomizations() {
        DraftOrdersUploadedTemplate template = DraftOrdersUploadedTemplate.builder()
            .subjectLineWithHearingDate(String.format("Davidson, %s, case management hearing, 3 November 2020",
                FAMILY_MAN_CASE_NUMBER))
            .respondentLastName("Davidson")
            .judgeTitle("Her Honour Judge")
            .judgeName("Judy")
            .caseUrl("http://fake-url/cases/case-details/" + CASE_ID + "#Draft%20orders")
            .draftOrders("Draft CMO from advocates' meeting\nBlank order")
            .build();
        return convert(template);
    }

    private CallbackRequest callbackRequest(CMOStatus status) {
        List<Element<HearingBooking>> hearingsBefore = hearings(HEARING_DATE);
        List<Element<HearingBooking>> hearings = hearings(
            HEARING_DATE,
            hearingsBefore.get(0).getId(),
            hearingsBefore.get(1).getId()
        );

        CaseData caseDataBefore = CaseData.builder()
            .hearingDetails(hearingsBefore)
            .draftUploadedCMOs(List.of())
            .build();

        Element<HearingOrder> order = element(order(hearings.get(0).getValue(), status));

        hearings.get(0).getValue().setCaseManagementOrderId(order.getId());

        Judge judy = Judge.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
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
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .build();

        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.setId(CASE_ID);

        return toCallBackRequest(caseDetails, asCaseDetails(caseDataBefore));
    }

    private HearingOrder order(HearingBooking hearing, CMOStatus status) {
        return HearingOrder.builder()
            .status(status)
            .hearing(hearing.toLabel())
            .order(TestDataHelper.testDocumentReference())
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

    private HearingOrder hearingOrder(HearingOrderType type, String title) {
        return HearingOrder.builder()
            .type(type)
            .title(title)
            .build();
    }
}
