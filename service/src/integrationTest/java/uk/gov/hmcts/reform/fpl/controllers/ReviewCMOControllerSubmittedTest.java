package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.orders.ReviewCMOController;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkThat;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ActiveProfiles("integration-test")
@WebMvcTest(ReviewCMOController.class)
@OverrideAutoConfiguration(enabled = true)
class ReviewCMOControllerSubmittedTest extends AbstractControllerTest {

    private static final long CASE_ID = 12345L;
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    private static final String ADMIN_EMAIL = "admin@family-court.com";
    private static final String CAFCASS_EMAIL = "cafcass@cafcass.com";
    private static final DocumentReference order = testDocumentReference();
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;
    private static final String SEND_DOCUMENT_EVENT = "internal-change-SEND_DOCUMENT";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    ReviewCMOControllerSubmittedTest() {
        super("review-cmo");
    }

    @Test
    void shouldNotSendNotificationsIfNoCMOsReadyForApproval() {
        CaseDetails caseDetails = CaseDetails.builder().data(
            Map.of("draftUploadedCMOs", List.of(element(buildCMO(RETURNED))))).build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails).build();

        postSubmittedEvent(callbackRequest);

        checkThat(() -> verify(notificationClient, never()).sendEmail(any(), any(), any(), any()));
    }

    @Test
    void shouldSendCMOIssuedNotificationsIfJudgeApproves() {
        given(documentDownloadService.downloadDocument(order.getBinaryUrl())).willReturn(DOCUMENT_CONTENT);

        HearingOrder caseManagementOrder = buildCMO(APPROVED);

        CaseDetails caseDetails = buildCaseDetailsForApprovedCMO(caseManagementOrder);

        CaseDetails caseDetailsBefore = CaseDetails.builder().data(
            Map.of("draftUploadedCMOs", List.of(element(buildCMO(SEND_TO_JUDGE))))).build();

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore).build();

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                eq(CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE),
                eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
                anyMap(),
                eq(NOTIFICATION_REFERENCE)
            );

            verify(notificationClient).sendEmail(
                eq(CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE),
                eq(CAFCASS_EMAIL),
                anyMap(),
                eq(NOTIFICATION_REFERENCE)
            );

            verify(notificationClient).sendEmail(
                eq(CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE),
                eq("robert@example.com"),
                anyMap(),
                eq(NOTIFICATION_REFERENCE)
            );

            verify(notificationClient).sendEmail(
                eq(CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE),
                eq("charlie@example.com"),
                anyMap(),
                eq(NOTIFICATION_REFERENCE)
            );

            verify(notificationClient).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
                eq(ADMIN_EMAIL),
                anyMap(),
                eq(NOTIFICATION_REFERENCE)
            );

            verify(coreCaseDataService).triggerEvent(JURISDICTION,
                CASE_TYPE,
                CASE_ID,
                SEND_DOCUMENT_EVENT,
                Map.of("documentToBeSent", caseManagementOrder.getOrder()));

            verifyNoMoreInteractions(notificationClient);
        });
    }

    @Test
    void shouldSendCMORejectedNotificationIfJudgeRequestedChanges() {
        CaseDetails caseDetails = buildCaseDetailsForRejectedCMO();
        caseDetails.setId(CASE_ID);

        CaseDetails caseDetailsBefore = CaseDetails.builder().data(
            Map.of("draftUploadedCMOs", List.of(element(buildCMO(SEND_TO_JUDGE))))).build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore).build();

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(CMO_REJECTED_BY_JUDGE_TEMPLATE),
            eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        ));

        verifyNoMoreInteractions(notificationClient);
    }

    private CaseDetails buildCaseDetailsForApprovedCMO(HearingOrder... caseManagementOrders) {
        UUID cmoId = UUID.randomUUID();
        UUID hearingId = UUID.randomUUID();
        CaseDetails caseDetails = asCaseDetails(CaseData.builder()
            .representatives(createRepresentatives())
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .draftUploadedCMOs(List.of(element(cmoId, buildCMO(SEND_TO_JUDGE))))
            .sealedCMOs(wrapElements(caseManagementOrders))
            .reviewCMODecision(buildReviewDecision(SEND_TO_ALL_PARTIES))
            .lastHearingOrderDraftsHearingId(hearingId)
            .hearingDetails(List.of(element(hearingId, hearing(cmoId))))
            .build());

        caseDetails.setId(CASE_ID);
        caseDetails.setJurisdiction(JURISDICTION);
        caseDetails.setCaseTypeId(CASE_TYPE);
        return caseDetails;
    }

    private CaseDetails buildCaseDetailsForRejectedCMO() {
        UUID cmoId = UUID.randomUUID();

        return asCaseDetails(CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .draftUploadedCMOs(List.of(element(cmoId, buildCMO(RETURNED))))
            .reviewCMODecision(buildReviewDecision(JUDGE_REQUESTED_CHANGES))
            .build());
    }

    private static HearingOrder buildCMO(CMOStatus status) {
        return HearingOrder.builder()
            .hearing("Test hearing 25th December 2020")
            .order(order)
            .status(status).build();
    }

    private static ReviewDecision buildReviewDecision(CMOReviewOutcome judgeDecision) {
        return ReviewDecision.builder()
            .decision(judgeDecision)
            .build();
    }

    private static HearingBooking hearing(UUID cmoId) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.now())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .caseManagementOrderId(cmoId)
            .build();
    }

    private static List<Element<Representative>> createRepresentatives() {
        return wrapElements(Representative.builder()
            .email("robert@example.com")
            .fullName("Robert Robin")
            .servingPreferences(DIGITAL_SERVICE)
            .build(), Representative.builder()
            .email("charlie@example.com")
            .fullName("Charlie Brown")
            .servingPreferences(EMAIL)
            .build());
    }
}
