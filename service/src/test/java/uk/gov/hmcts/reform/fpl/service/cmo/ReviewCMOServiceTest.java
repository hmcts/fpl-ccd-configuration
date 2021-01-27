package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.exceptions.HearingOrdersBundleNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FURTHER_CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;
import static uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement.EMPTY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
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
    private static final DocumentReference sealedOrder = testDocumentReference();
    private static final UUID cmoID = UUID.randomUUID();
    private static final Time TIME = new FixedTimeConfiguration().stoppedTime();
    private LocalDateTime futureDate;

    @Mock
    private DocumentSealingService documentSealingService;

    @Mock
    private DraftOrderService draftOrderService;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private ReviewCMOService service;

    @BeforeEach
    void setUp() {
        service = new ReviewCMOService(mapper, TIME, draftOrderService, documentSealingService);
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
            .orders(newArrayList(agreedCMO)).build());

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

        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(hearing1, newArrayList(cmo));

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

    @Test
    void shouldNotReturnErrorsWhenJudgeApprovesDraftCMO() {
        Element<HearingOrder> cmo = agreedCMO(hearing1);

        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(hearing1, newArrayList(cmo));

        ReviewDecision reviewDecision = ReviewDecision.builder()
            .decision(JUDGE_AMENDS_DRAFT).judgeAmendedDocument(DocumentReference.builder().build()).build();

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .reviewCMODecision(reviewDecision)
            .build();

        assertThat(service.validateReviewDecision(caseData, emptyMap())).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenJudgeReviewsAllOrdersAndReviewDecisionFieldsAreValid() {
        Element<HearingOrder> cmo = agreedCMO(hearing1);
        Element<HearingOrder> blankOrder = buildBlankOrder("Draft C21 order", hearing1);

        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(
            hearing1, asList(cmo, blankOrder));

        ReviewDecision reviewDecision = ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build();
        Map<String, Object> data = new HashMap<>();
        data.put("reviewDecision1", Map.of("decision", SEND_TO_ALL_PARTIES));

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .build();

        when(mapper.convertValue(anyMap(), eq(ReviewDecision.class))).thenReturn(reviewDecision);
        assertThat(service.validateReviewDecision(caseData, data)).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenJudgeAmendsDraftCMOAndJudgeAmendedDocumentIsMissing() {
        Element<HearingOrder> cmo = agreedCMO(hearing1);

        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(hearing1, newArrayList(cmo));

        ReviewDecision reviewDecision = ReviewDecision.builder().decision(JUDGE_AMENDS_DRAFT).build();

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .reviewCMODecision(reviewDecision)
            .build();

        assertThat(service.validateReviewDecision(caseData, emptyMap()))
            .containsOnly("CMO - new file not uploaded");
    }

    @Test
    void shouldReturnErrorWhenJudgeRequestsChangesForDraftCMOAndRequestedChangesIsEmpty() {
        Element<HearingOrder> cmo = agreedCMO(hearing1);

        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(hearing1, newArrayList(cmo));

        ReviewDecision reviewDecision = ReviewDecision.builder().decision(JUDGE_REQUESTED_CHANGES).build();

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .reviewCMODecision(reviewDecision)
            .build();

        assertThat(service.validateReviewDecision(caseData, emptyMap()))
            .containsOnly("CMO - complete the requested changes");
    }

    @Test
    void shouldNotReturnErrorWhenJudgeDoesNotReviewDraftCMOOrder() {
        Element<HearingOrder> cmo = agreedCMO(hearing1);

        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(hearing1, newArrayList(cmo));

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .reviewCMODecision(null)
            .build();

        assertThat(service.validateReviewDecision(caseData, emptyMap())).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenDraftOrdersBundleContainsCMOAndBlankOrdersAndJudgeReviewsOnlyCMO() {
        Element<HearingOrder> cmo = agreedCMO(hearing1);
        Element<HearingOrder> blankOrder = buildBlankOrder("Draft C21 order", hearing1);

        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(
            hearing1, asList(cmo, blankOrder));

        Map<String, Object> data = new HashMap<>();
        data.put("reviewDecision1", null);

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .build();

        assertThat(service.validateReviewDecision(caseData, data)).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenDraftCMOAndDraftBlankOrderReviewDecisionFieldsAreInvalid() {
        Element<HearingOrder> cmo = agreedCMO(hearing1);
        Element<HearingOrder> blankOrder = buildBlankOrder("Draft C21 order", hearing1);

        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(
            hearing1, asList(cmo, blankOrder));

        ReviewDecision reviewDecision = ReviewDecision.builder().decision(JUDGE_AMENDS_DRAFT).build();
        Map<String, Object> data = new HashMap<>();
        data.put("reviewDecision1", Map.of("decision", JUDGE_AMENDS_DRAFT));

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .reviewCMODecision(ReviewDecision.builder().decision(JUDGE_REQUESTED_CHANGES).build())
            .build();

        when(mapper.convertValue(anyMap(), eq(ReviewDecision.class))).thenReturn(reviewDecision);
        assertThat(service.validateReviewDecision(caseData, data))
            .contains("CMO - complete the requested changes", "Order 1 - new file not uploaded");
    }

    @Test
    void shouldReturnErrorsForInvalidReviewDecisionOrdersWhenJudgeReviewsOrdersAndOneOrderReviewDecisionIsInvalid() {
        Element<HearingOrder> cmo = draftCMO(hearing1);
        Element<HearingOrder> blankOrder = buildBlankOrder("Draft C21 order", hearing1);

        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(
            hearing1, asList(cmo, blankOrder));

        ReviewDecision reviewDecision = ReviewDecision.builder().decision(JUDGE_REQUESTED_CHANGES).build();
        Map<String, Object> data = new HashMap<>();
        data.put("reviewDecision1", Map.of("decision", JUDGE_REQUESTED_CHANGES));

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .build();

        when(mapper.convertValue(anyMap(), eq(ReviewDecision.class))).thenReturn(reviewDecision);
        assertThat(service.validateReviewDecision(caseData, data))
            .contains("Order 1 - complete the requested changes");
    }

    @ParameterizedTest
    @MethodSource("cmoReviewDecisionData")
    void shouldReturnCMOToSealWithOriginalDocumentAndSetCaseStateWhenJudgeSelectsSealAndSend(
        String name,
        List<Element<HearingBooking>> hearingBookings, State expectedState) {
        Element<HearingOrder> agreedCMO = element(cmoID, HearingOrder.builder()
            .hearing(hearing1)
            .title(hearing1)
            .type(AGREED_CMO)
            .order(order)
            .status(SEND_TO_JUDGE)
            .judgeTitleAndName("Her Honour Judge Judy").build());

        Element<HearingOrdersBundle> ordersBundleElement = buildDraftOrdersBundle(hearing1, newArrayList(agreedCMO));

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .hearingDetails(hearingBookings)
            .build();

        HearingOrder expectedCmo = expectedSealedCMO(order);

        Map<String, Object> expectedData = Map.of(
            "sealedCMOs", List.of(element(agreedCMO.getId(), expectedCmo)),
            "state", expectedState,
            "draftUploadedCMOs", emptyList(),
            "hearingOrdersBundlesDrafts", emptyList()
        );

        when(draftOrderService.migrateCmoDraftToOrdersBundles(any(CaseData.class)))
            .thenReturn(emptyList());

        when(documentSealingService.sealDocument(any())).thenReturn(sealedOrder);

        Map<String, Object> actualData = service.reviewCMO(caseData, ordersBundleElement);

        assertThat(actualData).containsAllEntriesOf(expectedData);
    }

    @Test
    void shouldReplaceTheOrderAndSealTheCMOWhenJudgeAmendsTheDocument() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);

        Element<HearingOrdersBundle> ordersBundleElement = buildDraftOrdersBundle(hearing1, newArrayList(agreedCMO));

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6), FINAL, agreedCMO.getId())),
            element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3), HearingType.CASE_MANAGEMENT,
                UUID.randomUUID())));

        DocumentReference judgeAmendedDocument = testDocumentReference();
        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
            .reviewCMODecision(ReviewDecision.builder().decision(JUDGE_AMENDS_DRAFT)
                .judgeAmendedDocument(judgeAmendedDocument).build())
            .hearingDetails(hearingBookings)
            .build();

        HearingOrder expectedCmo = expectedSealedCMO(judgeAmendedDocument);

        Map<String, Object> expectedData = Map.of(
            "sealedCMOs", List.of(element(agreedCMO.getId(), expectedCmo)),
            "state", State.CASE_MANAGEMENT,
            "draftUploadedCMOs", emptyList(),
            "hearingOrdersBundlesDrafts", emptyList()
        );

        when(draftOrderService.migrateCmoDraftToOrdersBundles(any(CaseData.class)))
            .thenReturn(emptyList());
        when(documentSealingService.sealDocument(any())).thenReturn(sealedOrder);

        Map<String, Object> actualData = service.reviewCMO(caseData, ordersBundleElement);

        assertThat(actualData).containsAllEntriesOf(expectedData);
    }

    @Test
    void shouldRemoveDraftCMOWhenJudgeRequestsChanges() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);

        Element<HearingOrdersBundle> ordersBundleElement = buildDraftOrdersBundle(hearing1, newArrayList(agreedCMO));

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
            .reviewCMODecision(ReviewDecision.builder().decision(JUDGE_REQUESTED_CHANGES)
                .changesRequestedByJudge("requested changes text").build())
            .build();

        Map<String, Object> expectedData = Map.of(
            "draftUploadedCMOs", emptyList(),
            "hearingOrdersBundlesDrafts", emptyList()
        );

        Map<String, Object> actualData = service.reviewCMO(caseData, ordersBundleElement);

        assertThat(actualData).containsAllEntriesOf(expectedData);
        assertThat(actualData).doesNotContainKeys("selectedCMOs", "state");
    }

    @ParameterizedTest
    @MethodSource("reviewDecisionForDraftOrders")
    void shouldSealTheDraftBlankOrderAndCreateBlankOrderWhenJudgeApprovesTheDraftOrder(
        ReviewDecision reviewDecision) {
        Element<HearingOrder> draftOrder1 = buildBlankOrder("test order1", hearing1);

        Element<HearingOrdersBundle> ordersBundleElement =
            buildDraftOrdersBundle(hearing1, newArrayList(draftOrder1));

        Map<String, Object> data = new HashMap<>();
        data.put("reviewDecision1", Map.of("decision", reviewDecision.getDecision()));

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(newArrayList(draftOrder1))
            .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
            .reviewCMODecision(reviewDecision)
            .orderCollection(newArrayList())
            .build();

        when(mapper.convertValue(anyMap(), eq(ReviewDecision.class)))
            .thenReturn(reviewDecision);

        DocumentReference documentToSeal = JUDGE_AMENDS_DRAFT.equals(reviewDecision.getDecision())
            ? reviewDecision.getJudgeAmendedDocument() : draftOrder1.getValue().getOrder();

        when(documentSealingService.sealDocument(eq(documentToSeal))).thenReturn(sealedOrder);

        GeneratedOrder expectedBlankOrder = expectedBlankOrder(draftOrder1.getValue().getTitle());
        Map<String, Object> expectedData = Map.of(
            "orderCollection", List.of(element(draftOrder1.getId(), expectedBlankOrder)),
            "hearingOrdersBundlesDrafts", emptyList()
        );

        service.reviewC21Orders(caseData, data, ordersBundleElement);
        assertThat(data).containsAllEntriesOf(expectedData);
    }

    @Test
    void shouldNotCreateBlankOrderWhenJudgeRequestsChanges() {
        Element<HearingOrder> draftOrder1 = buildBlankOrder("test order1", hearing1);

        Element<HearingOrdersBundle> ordersBundleElement =
            buildDraftOrdersBundle(hearing1, newArrayList(draftOrder1));

        ReviewDecision reviewDecision = ReviewDecision.builder().decision(JUDGE_REQUESTED_CHANGES)
            .judgeAmendedDocument(order).build();

        Map<String, Object> data = new HashMap<>();
        data.put("reviewDecision1", Map.of("decision", JUDGE_REQUESTED_CHANGES));

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(newArrayList(draftOrder1))
            .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().reviewDecision1(reviewDecision).build())
            .orderCollection(newArrayList())
            .build();

        when(mapper.convertValue(anyMap(), eq(ReviewDecision.class))).thenReturn(reviewDecision);

        Map<String, Object> expectedData = Map.of(
            "orderCollection", emptyList(),
            "hearingOrdersBundlesDrafts", emptyList()
        );
        service.reviewC21Orders(caseData, data, ordersBundleElement);
        assertThat(data).containsAllEntriesOf(expectedData);
    }

    @Test
    void shouldThrowAnExceptionWhenNoUpcomingHearingsAreAvailable() {
        Element<HearingOrder> agreedCMO = draftCMO(hearing2);
        Element<HearingOrdersBundle> ordersBundleElement =
            buildDraftOrdersBundle(hearing2, newArrayList(agreedCMO));

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .hearingDetails(emptyList())
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.reviewCMO(caseData, ordersBundleElement));

        assertThat(exception).hasMessageContaining("Failed to find hearing matching cmo id", cmoID);
    }

    @Test
    void shouldReturnCMOsThatAreReadyForApproval() {
        Element<HearingOrder> agreedCMO1 = element(HearingOrder.builder().status(SEND_TO_JUDGE).build());
        Element<HearingOrder> agreedCMO2 = element(HearingOrder.builder().status(SEND_TO_JUDGE).build());
        Element<HearingOrder> agreedCMO3 = element(HearingOrder.builder().status(RETURNED).build());
        Element<HearingOrder> agreedCMO4 = element(HearingOrder.builder().status(APPROVED).build());

        List<Element<HearingOrder>> draftCMOs = List.of(agreedCMO1, agreedCMO2, agreedCMO3, agreedCMO4);

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(draftCMOs)
            .build();

        List<Element<HearingOrder>> expectedCMOs = List.of(agreedCMO1, agreedCMO2);

        assertThat(service.getCMOsReadyForApproval(caseData)).isEqualTo(expectedCMOs);
    }

    @Test
    void shouldReturnTheSelectedHearingOrdersBundleFromDynamicList() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);
        Element<HearingOrder> blankOrder = buildBlankOrder("Draft C21 order", hearing2);

        Element<HearingOrdersBundle> selectedHearingBundle = buildDraftOrdersBundle(hearing1, newArrayList(agreedCMO));
        Element<HearingOrdersBundle> hearingBundle2 = buildDraftOrdersBundle(hearing2, newArrayList(blankOrder));

        DynamicList draftBundlesDynamicList = ElementUtils.asDynamicList(
            List.of(selectedHearingBundle, hearingBundle2), selectedHearingBundle.getId(), HearingOrdersBundle::getHearingName);

        CaseData caseData = CaseData.builder().draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(List.of(selectedHearingBundle, hearingBundle2))
            .cmoToReviewList(draftBundlesDynamicList)
            .build();

        when(mapper.convertValue(any(), eq(DynamicList.class))).thenReturn(draftBundlesDynamicList);

        assertThat(service.getSelectedHearingDraftOrdersBundle(caseData))
            .isEqualTo(selectedHearingBundle);
    }

    @Test
    void shouldReturnHearingOrdersBundleWhenOnlyOneDraftsBundleExists() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);

        Element<HearingOrdersBundle> hearingOrdersBundle = buildDraftOrdersBundle(hearing1, newArrayList(agreedCMO));

        DynamicList draftBundlesDynamicList = ElementUtils.asDynamicList(
            List.of(hearingOrdersBundle), hearingOrdersBundle.getId(), HearingOrdersBundle::getHearingName);

        CaseData caseData = CaseData.builder().draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .cmoToReviewList(draftBundlesDynamicList)
            .build();

        assertThat(service.getSelectedHearingDraftOrdersBundle(caseData))
            .isEqualTo(hearingOrdersBundle);
    }

    @Test
    void shouldThrowExceptionWhenSelectedHearingOrdersBundleIsNotFound() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);
        Element<HearingOrder> blankOrder = buildBlankOrder("Draft C21 order", hearing2);

        Element<HearingOrdersBundle> selectedHearingBundle = buildDraftOrdersBundle(hearing1, newArrayList(agreedCMO));
        Element<HearingOrdersBundle> hearingBundle2 = buildDraftOrdersBundle(hearing2, newArrayList(blankOrder));

        DynamicList draftBundlesDynamicList = ElementUtils.asDynamicList(
            List.of(selectedHearingBundle, hearingBundle2), UUID.randomUUID(), HearingOrdersBundle::getHearingName);

        CaseData caseData = CaseData.builder().draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(List.of(selectedHearingBundle, hearingBundle2))
            .cmoToReviewList(draftBundlesDynamicList)
            .build();

        when(mapper.convertValue(any(), eq(DynamicList.class))).thenReturn(draftBundlesDynamicList);

        assertThatExceptionOfType(HearingOrdersBundleNotFoundException.class).isThrownBy(
            () -> service.getSelectedHearingDraftOrdersBundle(caseData));
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
            .type(SEND_TO_JUDGE.equals(status) ? AGREED_CMO : DRAFT_CMO)
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
            .dateIssued(TIME.now().toLocalDate())
            .judgeTitleAndName("Her Honour Judge Judy").build());
    }

    private static Element<HearingOrdersBundle> buildDraftOrdersBundle(
        String hearing, List<Element<HearingOrder>> draftOrders) {
        return element(HearingOrdersBundle.builder()
            .hearingName(hearing)
            .orders(draftOrders)
            .judgeTitleAndName("Her Honour Judge Judy").build());
    }

    private static Stream<Arguments> reviewDecisionForDraftOrders() {
        DocumentReference amendedDocument = testDocumentReference();
        return Stream.of(
            Arguments.of(
                ReviewDecision.builder().decision(JUDGE_AMENDS_DRAFT).judgeAmendedDocument(amendedDocument).build()),
            Arguments.of(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build()));
    }

    private static Stream<Arguments> cmoReviewDecisionData() {
        LocalDateTime now = TIME.now();

        List<Element<HearingBooking>> hearingsWithNextHearingTypeFinal = List.of(
            element(createHearingBooking(now.plusDays(5), now.plusDays(6), FINAL, cmoID)),
            element(createHearingBooking(now.plusDays(2), now.plusDays(3), HearingType.CASE_MANAGEMENT,
                UUID.randomUUID())),
            element(createHearingBooking(now.plusDays(6), now.plusDays(7), FINAL,
                UUID.randomUUID())),
            element(createHearingBooking(now, now.plusDays(1), ISSUE_RESOLUTION, UUID.randomUUID())));

        List<Element<HearingBooking>> hearingsWithNextHearingTypeIssueResolution = List.of(
            element(createHearingBooking(now.plusDays(5), now.plusDays(6), FINAL, cmoID)),
            element(createHearingBooking(now.plusDays(2), now.plusDays(3), HearingType.CASE_MANAGEMENT,
                UUID.randomUUID())),
            element(createHearingBooking(now.plusDays(6), now.plusDays(7), ISSUE_RESOLUTION,
                UUID.randomUUID())),
            element(createHearingBooking(now, now.plusDays(1), ISSUE_RESOLUTION, UUID.randomUUID())));

        List<Element<HearingBooking>> hearingBookingsWithNextHearingTypeCaseManagement = List.of(
            element(createHearingBooking(now.plusDays(5), now.plusDays(6), FURTHER_CASE_MANAGEMENT,
                cmoID)),
            element(createHearingBooking(now.plusDays(6), now.plusDays(7), HearingType.CASE_MANAGEMENT,
                UUID.randomUUID())));

        return Stream.of(
            Arguments.of("next hearing type is FINAL",
                hearingsWithNextHearingTypeFinal, FINAL_HEARING),
            Arguments.of("next hearing type is ISSUE RESOLUTION",
                hearingsWithNextHearingTypeIssueResolution, State.CASE_MANAGEMENT),
            Arguments.of("next hearing type is ISSUE RESOLUTION",
                hearingBookingsWithNextHearingTypeCaseManagement, State.CASE_MANAGEMENT)
        );
    }

    private HearingOrder expectedSealedCMO(DocumentReference judgeAmendedOrder) {
        return HearingOrder.builder()
            .title(hearing1)
            .order(sealedOrder)
            .lastUploadedOrder(judgeAmendedOrder)
            .hearing(hearing1)
            .dateIssued(TIME.now().toLocalDate())
            .judgeTitleAndName("Her Honour Judge Judy")
            .status(APPROVED)
            .type(AGREED_CMO)
            .build();
    }

    private GeneratedOrder expectedBlankOrder(String title) {
        return GeneratedOrder.builder()
            .type(BLANK_ORDER.getLabel())
            .title(title)
            .document(sealedOrder)
            .dateOfIssue(formatLocalDateToString(TIME.now().toLocalDate(), DATE))
            .children(emptyList())
            .date(formatLocalDateTimeBaseUsingFormat(TIME.now(), TIME_DATE))
            .build();
    }
}
