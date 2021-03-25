package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.orders.ApproveDraftOrdersController;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.SendLetterService;
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
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_APPROVES_DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_REJECTS_DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkThat;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(ApproveDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ApproveDraftOrdersControllerSubmittedTest extends AbstractCallbackTest {

    private static final long CASE_ID = 12345L;
    private static final String ADMIN_EMAIL = "admin@family-court.com";
    private static final String CAFCASS_EMAIL = "cafcass@cafcass.com";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;
    private static final String SEND_DOCUMENT_EVENT = "internal-change-SEND_DOCUMENT";
    private static final String UPDATE_CASE_SUMMARY_EVENT = "internal-update-case-summary";
    private static final String FAMILY_MAN_CASE_NUMBER = "FM001";
    private static final DocumentReference orderDocumentCmo = testDocumentReference("cmo.pdf");
    private static final DocumentReference orderDocumentC21 = testDocumentReference("c21.pdf");

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private SendLetterService sendLetters;

    ApproveDraftOrdersControllerSubmittedTest() {
        super("approve-draft-orders");
    }

    @Test
    void shouldNotSendNotificationsIfNoCMOsReadyForApproval() {
        CaseDetails caseDetails = CaseDetails.builder().data(
            Map.of("ordersToBeSent", List.of())).build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails).build();

        postSubmittedEvent(callbackRequest);

        checkThat(() -> verify(notificationClient, never()).sendEmail(any(), any(), any(), any()));
    }

    @Test
    void shouldSendCMOIssuedNotificationsIfJudgeApproves() {
        given(documentDownloadService.downloadDocument(orderDocumentCmo.getBinaryUrl())).willReturn(DOCUMENT_CONTENT);

        HearingOrder caseManagementOrder = buildOrder(AGREED_CMO, APPROVED, orderDocumentCmo);

        CaseDetails caseDetails = buildCaseDetails(caseManagementOrder);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                eq(CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE),
                eq(LOCAL_AUTHORITY_1_INBOX),
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
    void shouldSendDraftOrdersIssuedNotificationsIfJudgeApprovesMultipleOrders() {
        given(documentDownloadService.downloadDocument(orderDocumentCmo.getBinaryUrl())).willReturn(DOCUMENT_CONTENT);
        given(documentDownloadService.downloadDocument(orderDocumentC21.getBinaryUrl())).willReturn(DOCUMENT_CONTENT);

        HearingOrder cmo = buildOrder(AGREED_CMO, APPROVED, orderDocumentCmo);
        HearingOrder c21 = buildOrder(C21, APPROVED, orderDocumentC21);

        CaseDetails caseDetails = buildCaseDetails(cmo, c21);

        final List<Recipient> recipients = List.of(createRespondentParty());

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                eq(JUDGE_APPROVES_DRAFT_ORDERS),
                eq(LOCAL_AUTHORITY_1_INBOX),
                anyMap(),
                eq(NOTIFICATION_REFERENCE)
            );

            verify(notificationClient).sendEmail(
                eq(JUDGE_APPROVES_DRAFT_ORDERS),
                eq(CAFCASS_EMAIL),
                anyMap(),
                eq(NOTIFICATION_REFERENCE)
            );

            verify(notificationClient).sendEmail(
                eq(JUDGE_APPROVES_DRAFT_ORDERS),
                eq("robert@example.com"),
                anyMap(),
                eq(NOTIFICATION_REFERENCE)
            );

            verify(notificationClient).sendEmail(
                eq(JUDGE_APPROVES_DRAFT_ORDERS),
                eq("charlie@example.com"),
                anyMap(),
                eq(NOTIFICATION_REFERENCE)
            );

            verify(notificationClient).sendEmail(
                eq(JUDGE_APPROVES_DRAFT_ORDERS),
                eq(ADMIN_EMAIL),
                anyMap(),
                eq(NOTIFICATION_REFERENCE)
            );

            verify(coreCaseDataService).triggerEvent(eq(JURISDICTION),
                eq(CASE_TYPE),
                eq(CASE_ID),
                eq(UPDATE_CASE_SUMMARY_EVENT),
                anyMap()
            );

            verify(sendLetters).send(
                cmo.getOrder(),
                recipients,
                CASE_ID,
                FAMILY_MAN_CASE_NUMBER
            );

            verify(sendLetters).send(
                c21.getOrder(),
                recipients,
                CASE_ID,
                FAMILY_MAN_CASE_NUMBER
            );

            verifyNoMoreInteractions(notificationClient);
            verifyNoMoreInteractions(sendLetters);
        });
    }

    @Test
    void shouldSendCMORejectedNotificationIfJudgeRequestedChanges() {
        CaseDetails caseDetails = buildCaseDetails(buildOrder(AGREED_CMO, RETURNED, orderDocumentCmo));
        caseDetails.setId(CASE_ID);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(CMO_REJECTED_BY_JUDGE_TEMPLATE),
            eq(LOCAL_AUTHORITY_1_INBOX),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        ));

        verifyNoMoreInteractions(notificationClient);
    }

    @Test
    void shouldSendDraftOrdersRejectedNotificationIfJudgeRequestedChangesOnMultipleOrders() {
        CaseDetails caseDetails = buildCaseDetails(buildOrder(AGREED_CMO, RETURNED, orderDocumentCmo),
            buildOrder(C21, RETURNED, orderDocumentC21));
        caseDetails.setId(CASE_ID);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(JUDGE_REJECTS_DRAFT_ORDERS),
            eq(LOCAL_AUTHORITY_1_INBOX),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        ));

        verifyNoMoreInteractions(notificationClient);
    }

    private CaseDetails buildCaseDetails(HearingOrder... caseManagementOrders) {
        UUID cmoId = UUID.randomUUID();
        UUID hearingId = UUID.randomUUID();

        CaseDetails caseDetails = asCaseDetails(CaseData.builder()
            .representatives(createRepresentatives())
            .respondents1(createNonRepresentedRespondents())
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .ordersToBeSent(wrapElements(caseManagementOrders))
            .lastHearingOrderDraftsHearingId(hearingId)
            .hearingDetails(List.of(element(hearingId, hearing(cmoId))))
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .build());

        caseDetails.setId(CASE_ID);
        caseDetails.setJurisdiction(JURISDICTION);
        caseDetails.setCaseTypeId(CASE_TYPE);
        return caseDetails;
    }

    private HearingOrder buildOrder(HearingOrderType type, CMOStatus status, DocumentReference orderDocument) {
        return HearingOrder.builder()
            .type(type)
            .status(status)
            .order(orderDocument)
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
            .endDate(LocalDateTime.now())
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

    private static List<Element<Respondent>> createNonRepresentedRespondents() {
        return wrapElements(
            Respondent.builder().party(createRespondentParty()).build()
        );
    }

    private static RespondentParty createRespondentParty() {
        return RespondentParty.builder()
            .firstName("John")
            .lastName("Smith")
            .address(Address.builder()
                .addressLine1("Somewhere over the rainbow")
                .postcode("RA1 9BW")
                .build())
            .build();
    }
}
