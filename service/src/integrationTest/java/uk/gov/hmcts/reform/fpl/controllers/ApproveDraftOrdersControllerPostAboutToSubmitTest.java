package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.orders.ApproveDraftOrdersController;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.event.CaseProgressionReportEventData;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(ApproveDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ApproveDraftOrdersControllerPostAboutToSubmitTest extends AbstractCallbackTest {

    @MockBean
    private DocumentSealingService documentSealingService;

    private final HearingOrder cmo = buildDraftOrder(AGREED_CMO);
    private final HearingOrder draftOrder = buildDraftOrder(C21);
    private final String hearing = "Test hearing 21st August 2020";
    private final DocumentReference convertedDocument = DocumentReference.builder().filename("converted").build();
    private final DocumentReference sealedDocument = DocumentReference.builder().filename("sealed").build();

    ApproveDraftOrdersControllerPostAboutToSubmitTest() {
        super("approve-draft-orders/post-submit-callback");
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
            .reviewCMODecision(reviewDecision)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getDraftUploadedCMOs()).isEmpty();
        assertThat(responseData.getHearingOrdersBundlesDrafts()).isEmpty();
        assertThat(responseData.getReviewCMODecision()).isEqualTo(reviewDecision);
        assertThat(responseData.getOrdersToBeSent()).containsOnly(
            element(cmoElement.getId(),
                cmo.toBuilder().status(RETURNED).requestedChanges("Please change XYZ").build())
        );
    }

    @Test
    void shouldSealPDFAndAddToSealedCMOsListAndSaveUnsealedCMOWhenJudgeApprovesOrders() {
        DocumentReference order = cmo.getOrder();
        UUID cmoId = UUID.randomUUID();

        Element<HearingOrder> cmoElement = element(cmoId, cmo);
        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            UUID.randomUUID(), newArrayList(cmoElement));
        Court court = Court.builder().build();
        given(documentSealingService.sealDocument(order, court, SealType.ENGLISH)).willReturn(sealedDocument);

        Selector othersSelector = Selector.newSelector(2);
        othersSelector.setSelected(List.of(0, 1));
        CaseData caseData = CaseData.builder()
            .court(court)
            .state(State.CASE_MANAGEMENT)
            .ordersToBeSent(List.of(element(HearingOrder.builder().build())))
            .others(Others.builder().firstOther(
                    Other.builder().name("Tim Jones").address(Address.builder().postcode("SE1").build()).build())
                .additionalOthers(wrapElements(Other.builder().name("Stephen Jones")
                    .address(Address.builder().postcode("SW2").build()).build())).build())
            .othersSelector(othersSelector)
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .draftUploadedCMOs(List.of(element(cmoId, cmo)))
            .hearingDetails(List.of(element(hearing(cmoId))))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .ordersToBeSent(List.of(element(HearingOrder.builder().build()))) // should be reset
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        CaseData responseData = extractCaseData(response);

        assertThat(responseData.getDraftUploadedCMOs()).isEmpty();

        assertThat(responseData.getSealedCMOs().size()).isEqualTo(1);
        Element<HearingOrder> sealedCMO = responseData.getSealedCMOs().get(0);
        assertThat(sealedCMO.getId()).isEqualTo(cmoElement.getId());
        assertThat(sealedCMO.getValue())
            .extracting("order", "lastUploadedOrder", "dateIssued", "status")
            .contains(sealedDocument, order, LocalDate.now(), APPROVED);

        assertThat(responseData.getOrdersToBeSent().size()).isEqualTo(1);
        Element<HearingOrder> orderToBeSent = responseData.getOrdersToBeSent().get(0);
        assertThat(orderToBeSent.getId()).isEqualTo(cmoElement.getId());
        assertThat(orderToBeSent.getValue())
            .extracting("order", "lastUploadedOrder", "dateIssued", "status")
            .contains(sealedDocument, order, LocalDate.now(), APPROVED);

        assertThat(sealedCMO.getValue().getOthersNotified()).contains("Tim Jones, Stephen Jones");
        assertThat(unwrapElements(sealedCMO.getValue().getOthers()))
            .contains(caseData.getOthers().getFirstOther(),
                caseData.getOthers().getAdditionalOthers().get(0).getValue());

        assertThat(orderToBeSent.getValue().getOthersNotified()).contains("Tim Jones, Stephen Jones");
        assertThat(unwrapElements(orderToBeSent.getValue().getOthers()))
            .contains(caseData.getOthers().getFirstOther(),
                caseData.getOthers().getAdditionalOthers().get(0).getValue());
        assertTemporaryFieldsAreRemovedFromCaseData(response.getData());
    }

    @ParameterizedTest
    @MethodSource("populateCaseDataWithState")
    void shouldUpdateCaseStateWhenCmoDecisionIsSendToAllParties(
        String testName, HearingType hearingType, State expectedCaseState) {
        Court court = Court.builder().build();
        given(documentSealingService.sealDocument(convertedDocument, court, SealType.ENGLISH))
                .willReturn(sealedDocument);

        UUID cmoId = UUID.randomUUID();
        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            UUID.randomUUID(), newArrayList(element(cmoId, cmo)));

        CaseData caseData = CaseData.builder()
            .court(court)
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(List.of(element(cmoId, cmo)))
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .hearingDetails(List.of(
                element(hearing(cmoId)),
                element(buildHearingOfType(hearingType))))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(expectedCaseState).isEqualTo(responseData.getState());
    }

    @Test
    void shouldNotModifyDataIfNoDraftCMOsAreReviewedReadyForApproval() {
        ArrayList<Element<HearingOrder>> draftCMOs = newArrayList(element(cmo));
        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            UUID.randomUUID(), draftCMOs);

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(draftCMOs)
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .lastHearingOrderDraftsHearingId(hearingOrdersBundle.getValue().getHearingId())
            // required due to the json unwrapping
            .uploadDraftOrdersEventData(UploadDraftOrdersData.builder().build())
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().build())
            .caseProgressionReportEventData(CaseProgressionReportEventData.builder().build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData).isEqualTo(caseData);
    }

    @ParameterizedTest
    @EnumSource(value = CMOReviewOutcome.class, names = {"JUDGE_AMENDS_DRAFT", "SEND_TO_ALL_PARTIES"})
    void shouldSealPDFAndCreateBlankOrderWhenJudgeApprovesBlankOrders(CMOReviewOutcome reviewOutcome) {
        DocumentReference order = draftOrder.getOrder();
        UUID draftOrderId = UUID.randomUUID();
        Court court = Court.builder().build();
        Element<HearingOrder> orderElement = element(draftOrderId, draftOrder);
        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            UUID.randomUUID(), newArrayList(orderElement));

        if (SEND_TO_ALL_PARTIES.equals(reviewOutcome)) {
            given(documentSealingService.sealDocument(order, court, SealType.ENGLISH))
                    .willReturn(sealedDocument);
        } else {
            given(documentSealingService.sealDocument(convertedDocument, court, SealType.ENGLISH))
                    .willReturn(sealedDocument);
        }

        ReviewDecision reviewDecision =
            reviewOutcome.equals(SEND_TO_ALL_PARTIES) ? ReviewDecision.builder().decision(reviewOutcome).build()
                : ReviewDecision.builder().decision(reviewOutcome).judgeAmendedDocument(convertedDocument).build();

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = buildJudgeAndLegalAdvisor();
        Judge allocatedJudge = buildJudge();

        CaseData caseData = CaseData.builder()
            .court(court)
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .draftUploadedCMOs(newArrayList())
            .children1(children())
            .hearingDetails(List.of(element(
                hearingOrdersBundle.getValue().getHearingId(),
                HearingBooking.builder().judgeAndLegalAdvisor(judgeAndLegalAdvisor).build())))
            .allocatedJudge(allocatedJudge)
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().reviewDecision1(reviewDecision).build())
            .build();

        JudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor = expectedJudgeAndLegalAdviser(
            judgeAndLegalAdvisor, allocatedJudge);

        Element<HearingOrder> expectedOrderToReturn = element(draftOrderId,
            draftOrder.toBuilder().status(APPROVED).order(sealedDocument)
                .others(null).othersNotified("")
                .lastUploadedOrder(SEND_TO_ALL_PARTIES.equals(reviewOutcome) ? order : convertedDocument).build());

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getHearingOrdersBundlesDrafts()).isEmpty();
        assertThat(responseData.getOrderCollection()).isNotEmpty();
        assertThat(responseData.getOrdersToBeSent()).containsOnly(expectedOrderToReturn);

        assertThat(responseData.getOrderCollection().get(0).getValue())
            .extracting("type", "title", "document", "children", "judgeAndLegalAdvisor")
            .containsExactlyInAnyOrder(BLANK_ORDER.getLabel(), draftOrder.getTitle(), sealedDocument,
                caseData.getChildren1(), expectedJudgeAndLegalAdvisor);
    }

    @Test
    void shouldRemoveRejectedBlankOrderAndSealApprovedOrderWhenJudgeRejectsOneOrderAndApprovesTheOther() {
        DocumentReference order = cmo.getOrder();
        Court court = Court.builder().build();
        given(documentSealingService.sealDocument(order, court, SealType.ENGLISH)).willReturn(sealedDocument);

        UUID cmoId = UUID.randomUUID();
        UUID draftOrderId = UUID.randomUUID();
        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            UUID.randomUUID(), newArrayList(element(cmoId, cmo), element(draftOrderId, draftOrder)));

        CaseData caseData = CaseData.builder()
            .court(court)
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
            .others(null)
            .othersNotified("")
            .build();

        HearingOrder expectedRejectedOrder = draftOrder.toBuilder()
            .status(RETURNED).requestedChanges("missing data").build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getDraftUploadedCMOs()).isEmpty();
        assertThat(responseData.getSealedCMOs()).containsOnly(element(cmoId, expectedSealedCmo));
        assertThat(responseData.getOrdersToBeSent()).containsOnly(element(cmoId, expectedSealedCmo),
            element(draftOrderId, expectedRejectedOrder));

        assertThat(responseData.getOrderCollection()).isEmpty();
        assertThat(responseData.getHearingOrdersBundlesDrafts()).isEmpty();
        assertThat(responseData.getDraftUploadedCMOs()).isEmpty();
    }

    @Test
    void shouldReviewDraftOrdersOnlyWhenHearingBundlesForApprovalExists() {
        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .hearingDetails(emptyList())
            .draftUploadedCMOs(newArrayList())
            .hearingOrdersBundlesDrafts(emptyList())
            .ordersToBeSent(List.of(element(HearingOrder.builder().build())))
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getSealedCMOs()).isEmpty();
        assertThat(responseData.getOrderCollection()).isEmpty();
        assertThat(responseData.getOrdersToBeSent()).isNull();
    }

    private void assertTemporaryFieldsAreRemovedFromCaseData(Map<String, Object> responseData) {
        assertThat(responseData).doesNotContainKeys("numDraftCMOs", "cmoToReviewList", "draftCMOExists",
            "draftBlankOrdersCount", "cmoDraftOrderTitle", "draftOrder1Title", "draftOrder2Title",
            "draftOrder3Title", "draftOrder4Title", "draftOrder5Title", "draftOrder6Title", "draftOrder7Title",
            "draftOrder8Title", "draftOrder9Title", "draftOrder10Title", "cmoDraftOrderDocument",
            "draftOrder1Document", "draftOrder2Document", "draftOrder3Document", "draftOrder4Document",
            "draftOrder5Document", "draftOrder6Document", "draftOrder7Document", "draftOrder8Document",
            "draftOrder9Document", "draftOrder10Document", "reviewDraftOrdersTitles", "draftOrdersTitlesInBundle");
    }

    private static Stream<Arguments> populateCaseDataWithState() {
        return Stream.of(
            Arguments.of("Next hearing type is issue resolution", ISSUE_RESOLUTION, State.CASE_MANAGEMENT),
            Arguments.of("Next hearing type is final", FINAL, FINAL_HEARING)
        );
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
            .judgeAndLegalAdvisor(buildJudgeAndLegalAdvisor())
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
        return List.of(element(
            Child.builder()
                .party(ChildParty.builder().build())
                .build()));
    }

    private JudgeAndLegalAdvisor expectedJudgeAndLegalAdviser(
        JudgeAndLegalAdvisor judgeAndLegalAdvisor, Judge allocatedJudge) {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(allocatedJudge.getJudgeTitle())
            .otherTitle(allocatedJudge.getOtherTitle())
            .judgeLastName(allocatedJudge.getJudgeLastName())
            .judgeFullName(allocatedJudge.getJudgeFullName())
            .judgeEmailAddress(allocatedJudge.getJudgeEmailAddress())
            .legalAdvisorName(judgeAndLegalAdvisor.getLegalAdvisorName())
            .allocatedJudgeLabel(judgeAndLegalAdvisor.getAllocatedJudgeLabel())
            .useAllocatedJudge(judgeAndLegalAdvisor.getUseAllocatedJudge())
            .build();
    }
}
