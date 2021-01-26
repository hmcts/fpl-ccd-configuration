package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FURTHER_CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement.EMPTY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class ReviewCMOServiceTest {

    private static final String SINGLE = "SINGLE";
    private static final String MULTI = "MULTI";
    private static final String NONE = "NONE";
    private static final String hearing1 = "Case management hearing, 2 March 2020";
    private static final String hearing2 = "Test hearing, 15 October 2020";
    private static final DocumentReference order = testDocumentReference();
    private final UUID cmoID = UUID.randomUUID();
    private static final Time TIME = new FixedTimeConfiguration().stoppedTime();
    private LocalDateTime futureDate;

    @Mock
    private DocumentSealingService documentSealingService;

    @Mock
    private DraftOrderService draftOrderService;

    private ReviewCMOService service;

    @BeforeEach
    void setUp() {
        service = new ReviewCMOService(new ObjectMapper(), TIME, draftOrderService, documentSealingService);
        futureDate = TIME.now().plusDays(1);
    }

    @Test
    void shouldBuildDynamicListWithAppropriateElementSelected() {
        Element<HearingOrdersBundle> hearingOrdersBundle1 = element(HearingOrdersBundle.builder()
            .orders(List.of(agreedCMO(hearing1))).build());

        Element<HearingOrdersBundle> hearingOrdersBundle2 = element(HearingOrdersBundle.builder()
            .orders(List.of(agreedCMO(hearing2), element(HearingOrder.builder().build()))).build());

        List<Element<HearingOrdersBundle>> hearingOrderBundlesDrafts = List.of(
            hearingOrdersBundle1, hearingOrdersBundle2
        );

        UUID selectedBundleId = hearingOrdersBundle1.getId();

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(hearingOrderBundlesDrafts)
            //This replicates bug in CCD which sends only String UUID in mid event
            .cmoToReviewList(selectedBundleId.toString())
            .numDraftCMOs("MULTI")
            .build();

        DynamicList actualDynamicList = service.buildDynamicList(caseData);

        DynamicList expectedDynamicList = ElementUtils
            .asDynamicList(hearingOrderBundlesDrafts, selectedBundleId, HearingOrdersBundle::getHearingName);

        assertThat(actualDynamicList).isEqualTo(expectedDynamicList);
    }

    @Test
    void shouldBuildUnselectedDynamicList() {
        Element<HearingOrdersBundle> hearingOrdersBundle1 = element(HearingOrdersBundle.builder()
            .hearingName(hearing1)
            .orders(List.of(agreedCMO(hearing1))).build());

        Element<HearingOrdersBundle> hearingOrdersBundle2 = element(HearingOrdersBundle.builder()
            .hearingName(hearing2)
            .orders(List.of(agreedCMO(hearing2), element(HearingOrder.builder().build()))).build());

        List<Element<HearingOrdersBundle>> hearingOrderBundlesDrafts = List.of(
            hearingOrdersBundle1, hearingOrdersBundle2
        );

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(hearingOrderBundlesDrafts)
            .build();

        DynamicList actualDynamicList = service.buildUnselectedDynamicList(caseData);
        DynamicList expectedDynamicList = ElementUtils
            .asDynamicList(hearingOrderBundlesDrafts, HearingOrdersBundle::getHearingName);

        assertThat(actualDynamicList).isEqualTo(expectedDynamicList);
    }

    @Test
    void shouldReturnMultiPageDataWhenThereAreMultipleDraftBundlesReadyForApproval() {
        Element<HearingOrdersBundle> hearingOrdersBundle1 = element(HearingOrdersBundle.builder()
            .orders(List.of(agreedCMO(hearing1))).build());

        Element<HearingOrdersBundle> hearingOrdersBundle2 = element(HearingOrdersBundle.builder()
            .orders(List.of(agreedCMO(hearing2), element(HearingOrder.builder().build()))).build());

        List<Element<HearingOrdersBundle>> hearingOrderBundlesDrafts = List.of(
            hearingOrdersBundle1, hearingOrdersBundle2
        );

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(hearingOrderBundlesDrafts)
            .build();

        Map<String, Object> expectedData = Map.of(
            "numDraftCMOs", MULTI,
            "cmoToReviewList", DynamicList.builder()
                .value(EMPTY)
                .listItems(List.of(
                    DynamicListElement.builder().code(hearingOrdersBundle1.getId()).build(),
                    DynamicListElement.builder().code(hearingOrdersBundle2.getId()).build()))
                .build());

        assertThat(service.getPageDisplayControls(caseData)).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnSinglePageDataWhenThereIsOnlyOneHearingOrdersDraftBundleExists() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);
        Element<HearingOrdersBundle> hearingOrdersBundle = element(HearingOrdersBundle.builder()
            .orders(asList(agreedCMO)).build());

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .build();

        Map<String, Object> expectedData = Map.of(
            "numDraftCMOs", SINGLE,
            "cmoDraftOrderTitle", hearing1,
            "cmoDraftOrderDocument", order,
            "draftCMOExists", "Y"
        );

        Map<String, Object> actualData = service.getPageDisplayControls(caseData);
        assertThat(actualData).containsAllEntriesOf(expectedData);
        assertThat(actualData).doesNotContainKey("cmoToReviewList");
    }

    @Test
    void shouldReturnNonePageDataWhenThereAreNoDraftCMOsReadyForApproval() {
        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(emptyList())
            .build();

        Map<String, Object> expectedData = Map.of(
            "numDraftCMOs", NONE);

        assertThat(service.getPageDisplayControls(caseData)).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnDraftOrdersDataWhenSelectedHearingOrdersBundleHaveCMOAndDraftOrdersForApproval() {
        Element<HearingOrder> cmo = agreedCMO(hearing1);
        Element<HearingOrder> blankOrder = buildBlankOrder("Draft C21 order", hearing1);

        Element<HearingOrdersBundle> draftOrdersBundle =
            buildDraftOrdersBundle(hearing1, asList(cmo, blankOrder));

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .build();

        Map<String, Object> expectedData = Map.of(
            "cmoDraftOrderTitle", hearing1,
            "cmoDraftOrderDocument", order,
            "draftCMOExists", "Y",
            "draftOrder1Title", blankOrder.getValue().getTitle(),
            "draftOrder1Document", order,
            "draftBlankOrdersCount", "1");

        assertThat(service.populateDraftOrdersData(caseData)).containsAllEntriesOf(expectedData);
    }

    @Test
    void shouldReturnCMODraftOrderWhenSelectedHearingOrdersBundleHaveOnlyDraftCMOForApproval() {
        Element<HearingOrder> cmo = agreedCMO(hearing1);

        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(hearing1, asList(cmo));

        CaseData caseData = CaseData.builder()
            .caseName("case1")
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .cmoToReviewList(draftOrdersBundle.getId())
            .build();

        Map<String, Object> expectedData = Map.of(
            "cmoDraftOrderTitle", hearing1,
            "cmoDraftOrderDocument", order,
            "draftCMOExists", "Y"
        );

        Map<String, Object> actualData = service.populateDraftOrdersData(caseData);
        assertThat(actualData).containsAllEntriesOf(expectedData);
        assertThat(actualData).doesNotContainKeys("draftOrder1Title", "draftOrder1Document", "draftBlankOrdersCount");
    }

    @Test
    void shouldReturnDraftC21OrdersWhenSelectedHearingOrdersBundleHaveOnlyDraftC21OrdersForApproval() {
        Element<HearingOrder> blankOrder1 = buildBlankOrder("Draft C21 order1", hearing1);
        Element<HearingOrder> blankOrder2 = buildBlankOrder("Draft C21 order2", hearing1);

        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(
            hearing1, asList(blankOrder1, blankOrder2));

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .cmoToReviewList(draftOrdersBundle.getId())
            .build();

        Map<String, Object> expectedData = Map.of(
            "draftOrder1Title", blankOrder1.getValue().getTitle(),
            "draftOrder1Document", order,
            "draftOrder2Title", blankOrder2.getValue().getTitle(),
            "draftOrder2Document", order,
            "draftBlankOrdersCount", "12");

        Map<String, Object> actualData = service.populateDraftOrdersData(caseData);
        assertThat(actualData).containsAllEntriesOf(expectedData);
        assertThat(actualData).doesNotContainKeys("cmoDraftOrderTitle", "cmoDraftOrderDocument", "draftCMOExists");
    }


    // TODO: review tests

    @Test
    void shouldReturnCMOsThatAreReadyForApproval() {
        Element<HearingOrder> agreedCMO1 = element(HearingOrder.builder().status(SEND_TO_JUDGE).build());
        Element<HearingOrder> agreedCMO2 = element(HearingOrder.builder().status(SEND_TO_JUDGE).build());
        Element<HearingOrder> agreedCMO3 = element(HearingOrder.builder().status(RETURNED).build());
        Element<HearingOrder> agreedCMO4 = element(HearingOrder.builder().status(APPROVED).build());

        List<Element<HearingOrder>> draftCMOs = List.of(agreedCMO1, agreedCMO2, agreedCMO3, agreedCMO4);
        CaseData caseData = CaseData.builder().draftUploadedCMOs(draftCMOs).build();

        List<Element<HearingOrder>> expectedCMOs = List.of(agreedCMO1, agreedCMO2);

        assertThat(service.getCMOsReadyForApproval(caseData)).isEqualTo(expectedCMOs);
    }

    @Test
    @Disabled
    void shouldReturnCMOToSealWithOriginalDocumentWhenJudgeSelectsSealAndSend() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);

        /*CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(List.of(agreedCMO))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .hearingDetails(List.of(element(hearing(agreedCMO.getId())))).build();

        HearingOrder expectedCmo = HearingOrder.builder()
            .order(order)
            .hearing(hearing1)
            .dateIssued(time.now().toLocalDate())
            .judgeTitleAndName("Her Honour Judge Judy")
            .status(APPROVED)
            .build();*/

        //Element<HearingOrder> cmoToSeal = service.getCMOToSeal(caseData);

        /*assertThat(cmoToSeal.getValue()).isEqualTo(expectedCmo);
        assertThat(cmoToSeal.getId()).isEqualTo(agreedCMO.getId());*/
    }

    @Test
    void shouldReturnCMOToSealWithJudgeAmendedDocumentWhenJudgeSelectsMakeChanges() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);
        DocumentReference judgeAmendedOrder = testDocumentReference();

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(List.of(agreedCMO))
            .build();
            /*.reviewCMODecision(ReviewDecision.builder()
                .decision(JUDGE_AMENDS_DRAFT)
                .judgeAmendedDocument(judgeAmendedOrder)
                .build())
            .hearingDetails(List.of(element(hearing(agreedCMO.getId())))).build();*/

        HearingOrder expectedCmo = HearingOrder.builder()
            .order(judgeAmendedOrder)
            .hearing(hearing1)
            .dateIssued(TIME.now().toLocalDate())
            .judgeTitleAndName("Her Honour Judge Judy")
            .status(APPROVED)
            .build();

        /*Element<HearingOrder> cmoToSeal = service.getCMOToSeal(caseData);

        assertThat(cmoToSeal.getValue()).isEqualTo(expectedCmo);
        assertThat(cmoToSeal.getId()).isEqualTo(agreedCMO.getId());*/
    }

    @Test
    void shouldReturnCMOThatWasSelectedFromDynamicListWhenMultipleCMOsExist() {
        Element<HearingOrder> agreedCMO1 = agreedCMO(hearing1);
        Element<HearingOrder> agreedCMO2 = agreedCMO(hearing1);

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(List.of(agreedCMO1, agreedCMO2))
            .cmoToReviewList(DynamicList.builder()
                .value(DynamicListElement.builder().code(agreedCMO1.getId()).build())
                .listItems(dynamicListItems(agreedCMO1.getId(), agreedCMO2.getId())).build())
            .numDraftCMOs(MULTI)
            .build();

        assertThat(service.getSelectedCMO(caseData)).isEqualTo(agreedCMO1);
    }

    @Test
    void shouldReturnCMOFromDraftListWhenOnlyOneCMOExists() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);
        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(List.of(agreedCMO))
            .numDraftCMOs(SINGLE)
            .build();

        assertThat(service.getSelectedCMO(caseData)).isEqualTo(agreedCMO);
    }

    @Test
    void shouldIgnoreCMOsThatAreDraft() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);
        Element<HearingOrder> draftCMO = draftCMO(hearing2);

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(List.of(agreedCMO, draftCMO))
            .numDraftCMOs(SINGLE)
            .build();

        assertThat(service.getSelectedCMO(caseData)).isEqualTo(agreedCMO);
    }

    @Test
    void shouldGetLatestSealedCMOFromSealedCMOsList() {
        Element<HearingOrder> cmo1 = agreedCMO(hearing1);
        Element<HearingOrder> cmo2 = agreedCMO(hearing2);

        CaseData caseData = CaseData.builder().sealedCMOs(List.of(cmo1, cmo2)).build();

        assertThat(service.getLatestSealedCMO(caseData)).isEqualTo(cmo2.getValue());
    }

    @Test
    void shouldThrowExceptionIfSealedCMOListIsEmpty() {
        CaseData caseData = CaseData.builder().sealedCMOs(List.of()).build();
        assertThatExceptionOfType(CMONotFoundException.class).isThrownBy(
            () -> service.getLatestSealedCMO(caseData));
    }

    @Test
    void shouldReturnCaseManagementStateWhenNextHearingIsOfIssueResolutionType() {

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6), FINAL, cmoID)),
            element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3), CASE_MANAGEMENT,
                UUID.randomUUID())),
            element(createHearingBooking(futureDate.plusDays(6), futureDate.plusDays(7), ISSUE_RESOLUTION,
                UUID.randomUUID())),
            element(createHearingBooking(futureDate, futureDate.plusDays(1), ISSUE_RESOLUTION, UUID.randomUUID())));

        CaseData caseData = buildCaseData(SEND_TO_ALL_PARTIES, hearingBookings);
        assertThat(service.getStateBasedOnNextHearing(caseData, ReviewDecision.builder().build(), cmoID)).isEqualTo(State.CASE_MANAGEMENT);
    }

    @Test
    void shouldReturnFinalHearingStateWhenNextHearingIsOfFinalType() {

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6), FINAL, cmoID)),
            element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3), CASE_MANAGEMENT,
                UUID.randomUUID())),
            element(createHearingBooking(futureDate.plusDays(6), futureDate.plusDays(7), FINAL,
                UUID.randomUUID())),
            element(createHearingBooking(futureDate, futureDate.plusDays(1), ISSUE_RESOLUTION, UUID.randomUUID())));

        CaseData caseData = buildCaseData(SEND_TO_ALL_PARTIES, hearingBookings);
        assertThat(service.getStateBasedOnNextHearing(caseData, ReviewDecision.builder().build(), cmoID)).isEqualTo(State.FINAL_HEARING);
    }


    @Test
    void shouldReturnCaseManagementStateWhenNextHearingIsNotOfIssueResolutionOrFinalType() {

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6), FURTHER_CASE_MANAGEMENT,
                cmoID)),
            element(createHearingBooking(futureDate.plusDays(6), futureDate.plusDays(7), CASE_MANAGEMENT,
                UUID.randomUUID())));

        CaseData caseData = buildCaseData(SEND_TO_ALL_PARTIES, hearingBookings);
        assertThat(service.getStateBasedOnNextHearing(caseData, ReviewDecision.builder().build(), cmoID)).isEqualTo(State.CASE_MANAGEMENT);
    }

    @Test
    void shouldReturnCaseManagementStateWhenNextReviewDecisionIsNotSendToAllParties() {

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6), FINAL, cmoID)),
            element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3), CASE_MANAGEMENT,
                UUID.randomUUID())),
            element(createHearingBooking(futureDate.plusDays(6), futureDate.plusDays(7), ISSUE_RESOLUTION,
                UUID.randomUUID())),
            element(createHearingBooking(futureDate, futureDate.plusDays(1), ISSUE_RESOLUTION, UUID.randomUUID())));

        CaseData caseData = buildCaseData(JUDGE_AMENDS_DRAFT, hearingBookings);
        assertThat(service.getStateBasedOnNextHearing(caseData, ReviewDecision.builder().build(), cmoID)).isEqualTo(State.CASE_MANAGEMENT);
    }

    @Test
    void shouldThrowAnExceptionWhenNoUpcomingHearingsAreAvailable() {
        List<Element<HearingBooking>> hearingBookings = new ArrayList<>();
        CaseData caseData = buildCaseData(SEND_TO_ALL_PARTIES, hearingBookings);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.getStateBasedOnNextHearing(caseData, ReviewDecision.builder().build(), cmoID));

        assertThat(exception).hasMessageContaining("Failed to find hearing matching cmo id", cmoID);
    }

    private static Element<HearingOrder> draftCMO(String hearing) {
        return buildCMO(hearing, DRAFT);
    }

    private static Element<HearingOrder> agreedCMO(String hearing) {
        return buildCMO(hearing, SEND_TO_JUDGE);
    }

    private static Element<HearingOrder> buildCMO(String hearing, CMOStatus status) {
        return element(HearingOrder.builder()
            .hearing(hearing)
            .title(hearing)
            .type(AGREED_CMO.equals(status) ? AGREED_CMO : DRAFT_CMO)
            .order(order)
            .status(status)
            .judgeTitleAndName("Her Honour Judge Judy").build());
    }

    private static Element<HearingOrder> buildBlankOrder(String title, String hearing) {
        return element(HearingOrder.builder()
            .hearing(hearing)
            .title(title)
            .order(order)
            .type(HearingOrderType.C21)
            .status(SEND_TO_JUDGE)
            .judgeTitleAndName("Her Honour Judge Judy").build());
    }

    private static Element<HearingOrdersBundle> buildDraftOrdersBundle(
        String hearing, List<Element<HearingOrder>> draftOrders) {
        return element(HearingOrdersBundle.builder()
            .hearingName(hearing)
            .orders(draftOrders)
            .judgeTitleAndName("Her Honour Judge Judy").build());
    }

    private static List<DynamicListElement> dynamicListItems(UUID uuid1, UUID uuid2) {
        return new ArrayList<>(List.of(
            DynamicListElement.builder()
                .code(uuid1)
                .label("Case management hearing, 2 March 2020")
                .build(),
            DynamicListElement.builder()
                .code(uuid2)
                .label("Test hearing, 15 October 2020")
                .build()
        ));
    }

    private static HearingBooking hearing(UUID cmoId) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .caseManagementOrderId(cmoId)
            .startDate(LocalDate.of(2020, 3, 2).atTime(12, 0))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .build();
    }

    private CaseData buildCaseData(CMOReviewOutcome cmoReviewOutcome, List<Element<HearingBooking>> hearingDetails) {
        return CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            //.reviewCMODecision(ReviewDecision.builder().decision(cmoReviewOutcome).build())
            .hearingDetails(hearingDetails)
            .build();
    }
}
