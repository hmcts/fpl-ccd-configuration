package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.controllers.orders.ReviewCMOController;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ActiveProfiles("integration-test")
@WebMvcTest(ReviewCMOController.class)
@OverrideAutoConfiguration(enabled = true)
class ReviewCMOControllerAboutToSubmitTest extends AbstractControllerTest {

    @MockBean
    private DocumentSealingService documentSealingService;

    private final HearingOrder cmo = buildDraftOrder(AGREED_CMO);
    private final HearingOrder draftOrder = buildDraftOrder(C21);
    private final String hearing = "Test hearing 21st August 2020";
    private final DocumentReference convertedDocument = DocumentReference.builder().filename("converted").build();
    private final DocumentReference sealedDocument = DocumentReference.builder().filename("sealed").build();

    ReviewCMOControllerAboutToSubmitTest() {
        super("review-cmo");
    }

    @Test
    void shouldRemoveCMOFromDraftCMOsAndRequestedChangesWhenJudgeRejectsOrder() {
        UUID hearingOrdersBundleId = UUID.randomUUID();

        Element<HearingOrder> cmoElement = element(cmo);
        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            hearingOrdersBundleId, newArrayList(cmoElement));

        ReviewDecision reviewDecision = ReviewDecision.builder()
            .changesRequestedByJudge("Please change XYZ")
            .decision(JUDGE_REQUESTED_CHANGES)
            .build();

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .hearingDetails(emptyList())
            .draftUploadedCMOs(newArrayList(cmoElement))
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .cmoToReviewList(hearingOrdersBundleId.toString())
            .reviewCMODecision(reviewDecision).build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getDraftUploadedCMOs()).isEmpty();
        assertThat(responseData.getHearingOrdersBundlesDrafts()).isEmpty();
        assertThat(responseData.getReviewCMODecision()).isEqualTo(reviewDecision);
    }

    @Test
    void shouldSealPDFAndAddToSealedCMOsListAndSaveUnsealedCMOWhenJudgeApprovesOrders() {
        DocumentReference order = cmo.getOrder();
        UUID cmoId = UUID.randomUUID();

        Element<HearingOrder> cmoElement = element(cmoId, cmo);
        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            UUID.randomUUID(), newArrayList(cmoElement));

        given(documentSealingService.sealDocument(order)).willReturn(sealedDocument);

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .draftUploadedCMOs(List.of(element(cmoId, cmo)))
            .hearingDetails(List.of(element(hearing(cmoId))))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        HearingOrder expectedSealedCmo = cmo.toBuilder()
            .order(sealedDocument)
            .lastUploadedOrder(order)
            .dateIssued(LocalDate.now())
            .status(APPROVED)
            .build();

        assertThat(responseData.getDraftUploadedCMOs()).isEmpty();
        assertThat(responseData.getSealedCMOs())
            .extracting(Element::getValue)
            .containsExactly(expectedSealedCmo);
    }

    @Test
    void shouldKeepStateInCaseManagementWhenNextHearingTypeIsIssueResolutionAndCmoDecisionIsSendToAllParties() {
        given(documentSealingService.sealDocument(convertedDocument)).willReturn(sealedDocument);

        UUID cmoId = UUID.randomUUID();
        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            UUID.randomUUID(), newArrayList(element(cmoId, cmo)));

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(List.of(element(cmoId, cmo)))
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .hearingDetails(List.of(
                element(hearing(cmoId)),
                element(buildHearingOfType(ISSUE_RESOLUTION))))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(State.CASE_MANAGEMENT).isEqualTo(responseData.getState());
    }

    @Test
    void shouldUpdateStateToFinalHearingWhenNextHearingTypeIsFinalAndCmoDecisionIsSendToAllParties() {
        given(documentSealingService.sealDocument(convertedDocument)).willReturn(sealedDocument);

        UUID cmoId = UUID.randomUUID();

        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            UUID.randomUUID(), newArrayList(element(cmoId, cmo)));

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(List.of(element(cmoId, cmo)))
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .hearingDetails(List.of(
                element(hearing(cmoId)),
                element(buildHearingOfType(FINAL))))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .build();
        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(State.FINAL_HEARING).isEqualTo(responseData.getState());
    }

    @Test
    void shouldNotModifyDataIfNoDraftCMOsAreReviewedReadyForApproval() {
        ArrayList<Element<HearingOrder>> draftCMOs = newArrayList(element(cmo));
        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            UUID.randomUUID(), draftCMOs);

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(draftCMOs)
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            // required due to the json unwrapping
            .uploadDraftOrdersEventData(UploadDraftOrdersData.builder().build())
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData).isEqualTo(caseData);
    }

    @ParameterizedTest
    @EnumSource(value = CMOReviewOutcome.class, names = {"JUDGE_AMENDS_DRAFT", "SEND_TO_ALL_PARTIES"})
    void shouldSealPDFAndCreateBlankOrderWhenJudgeApprovesBlankOrders(CMOReviewOutcome reviewOutcome) {
        DocumentReference order = draftOrder.getOrder();
        UUID draftOrderId = UUID.randomUUID();

        Element<HearingOrder> orderElement = element(draftOrderId, draftOrder);
        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            UUID.randomUUID(), newArrayList(orderElement));

        if (SEND_TO_ALL_PARTIES.equals(reviewOutcome)) {
            given(documentSealingService.sealDocument(order)).willReturn(sealedDocument);
        } else {
            given(documentSealingService.sealDocument(convertedDocument)).willReturn(sealedDocument);
        }

        ReviewDecision reviewDecision =
            reviewOutcome.equals(SEND_TO_ALL_PARTIES) ? ReviewDecision.builder().decision(reviewOutcome).build()
                : ReviewDecision.builder().decision(reviewOutcome).judgeAmendedDocument(convertedDocument).build();

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .draftUploadedCMOs(newArrayList())
            .children1(children())
            .judgeAndLegalAdvisor(buildJudgeAndLegalAdvisor())
            .allocatedJudge(buildJudge())
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().reviewDecision1(reviewDecision).build())
            .build();

        JudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .allocatedJudgeLabel("Case assigned to: Her Honour Judge Judy").build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getHearingOrdersBundlesDrafts()).isEmpty();
        assertThat(responseData.getOrderCollection()).isNotEmpty();

        assertThat(responseData.getOrderCollection().get(0).getValue())
            .extracting("type", "title", "document", "judgeAndLegalAdvisor", "children")
            .containsExactlyInAnyOrder(BLANK_ORDER.getLabel(), draftOrder.getTitle(), sealedDocument,
                expectedJudgeAndLegalAdvisor, caseData.getAllChildren());
    }

    @Test
    void shouldRemoveRejectedBlankOrderAndSealApprovedOrderWhenJudgeRejectsOneOrderAndApprovesTheOther() {
        DocumentReference order = cmo.getOrder();
        given(documentSealingService.sealDocument(order)).willReturn(sealedDocument);

        UUID cmoId = UUID.randomUUID();
        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            UUID.randomUUID(), newArrayList(element(cmoId, cmo), element(UUID.randomUUID(), draftOrder)));

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(List.of(element(cmoId, cmo)))
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .hearingDetails(List.of(
                element(hearing(cmoId)),
                element(buildHearingOfType(ISSUE_RESOLUTION))))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder()
                .reviewDecision1(ReviewDecision.builder().decision(JUDGE_REQUESTED_CHANGES)
                    .changesRequestedByJudge("missing data").build()).build())
            .build();

        HearingOrder expectedSealedCmo = cmo.toBuilder()
            .order(sealedDocument)
            .lastUploadedOrder(order)
            .dateIssued(LocalDate.now())
            .status(APPROVED)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getDraftUploadedCMOs()).isEmpty();
        assertThat(responseData.getSealedCMOs())
            .extracting(Element::getValue)
            .containsExactly(expectedSealedCmo);

        assertThat(responseData.getOrderCollection()).isEmpty();
        assertThat(responseData.getHearingOrdersBundlesDrafts()).isEmpty();
        assertThat(responseData.getDraftUploadedCMOs()).isEmpty();
    }

    private Element<HearingOrdersBundle> buildHearingOrdersBundle(
        UUID hearingOrdersBundle1, List<Element<HearingOrder>> orders) {
        return element(hearingOrdersBundle1,
            HearingOrdersBundle.builder().hearingId(UUID.randomUUID())
                .orders(orders)
                .hearingName(hearing).build());
    }

    private HearingBooking buildHearingOfType(HearingType hearingType) {
        return HearingBooking.builder()
            .startDate(LocalDateTime.now().plusDays(1))
            .type(hearingType)
            .caseManagementOrderId(UUID.randomUUID())
            .build();
    }

    private HearingOrder buildDraftOrder(HearingOrderType orderType) {
        return HearingOrder.builder()
            .title(hearing)
            .hearing(hearing)
            .type(orderType)
            .order(testDocumentReference())
            .judgeTitleAndName("Her Honour Judge Judy")
            .dateIssued(LocalDate.now())
            .status(DRAFT_CMO.equals(orderType) ? DRAFT : SEND_TO_JUDGE).build();
    }

    private HearingBooking hearing(UUID cmoId) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.now())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .caseManagementOrderId(cmoId)
            .build();
    }

    private Judge buildJudge() {
        return Judge.builder().judgeTitle(HER_HONOUR_JUDGE).judgeLastName("Judy").build();
    }

    private JudgeAndLegalAdvisor buildJudgeAndLegalAdvisor() {
        return JudgeAndLegalAdvisor.builder()
            .useAllocatedJudge("Yes")
            .legalAdvisorName("Chris Newport")
            .build();
    }

    private List<Element<Child>> children() {
        return ElementUtils.wrapElements(
            Child.builder()
                .party(ChildParty.builder().build())
                .build());
    }
}
