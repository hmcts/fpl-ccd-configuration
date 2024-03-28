package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ConfidentialOrderBundle;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundles;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.REVIEW_LATER;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FURTHER_CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;
import static uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement.EMPTY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class ApproveDraftOrdersServiceTest {

    private static final String SINGLE = "SINGLE";
    private static final String MULTI = "MULTI";
    private static final String NONE = "NONE";
    private static final String hearing1 = "Case management hearing, 2 March 2020";
    private static final String hearing2 = "Test hearing, 15 October 2020";
    private static final DocumentReference order = testDocumentReference();
    private static final DocumentReference sealedOrder = testDocumentReference();
    private static final UUID cmoID = UUID.randomUUID();
    private static final Time TIME = new FixedTimeConfiguration().stoppedTime();
    private static final Map<String, Object> BUNDLE_DATA = Map.of(
        "bundleData",
        "x");

    @Mock
    private DraftOrderService draftOrderService;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private DraftOrdersReviewDataBuilder draftOrdersReviewDataBuilder;

    @Mock
    private ReviewDecisionValidator reviewDecisionValidator;

    @Mock
    private DraftOrdersBundleHearingSelector draftOrdersBundleHearingSelector;

    @Mock
    private BlankOrderGenerator blankOrderGenerator;

    @Mock
    private HearingOrderGenerator hearingOrderGenerator;

    @Mock
    private OthersService othersService;

    @InjectMocks
    private ApproveDraftOrdersService underTest;

    @BeforeEach
    void setUp() {
        underTest = new ApproveDraftOrdersService(
            mapper,
            draftOrderService,
            draftOrdersReviewDataBuilder,
            reviewDecisionValidator,
            draftOrdersBundleHearingSelector,
            blankOrderGenerator,
            hearingOrderGenerator,
            othersService
        );
    }

    @Test
    void shouldBuildDynamicListWithAppropriateElementSelected() {
        Element<HearingOrdersBundle> hearingOrdersBundle1 = element(
            HearingOrdersBundle.builder().orders(newArrayList(agreedCMO(hearing1))).build());

        List<Element<HearingOrdersBundle>> hearingOrderBundlesDrafts = List.of(hearingOrdersBundle1,
            element(HearingOrdersBundle.builder().orders(newArrayList(agreedCMO(hearing2))).build())
        );

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(hearingOrderBundlesDrafts)
            //This replicates bug in CCD which sends only String UUID in mid event
            .cmoToReviewList(hearingOrdersBundle1.getId().toString())
            .numDraftCMOs("MULTI")
            .build();

        given(draftOrdersBundleHearingSelector.getSelectedHearingDraftOrdersBundle(caseData))
            .willReturn(hearingOrdersBundle1);

        DynamicList expectedDynamicList = ElementUtils.asDynamicList(
            hearingOrderBundlesDrafts, hearingOrdersBundle1.getId(), HearingOrdersBundle::getHearingName);

        assertThat(underTest.buildDynamicList(caseData)).isEqualTo(expectedDynamicList);
    }

    @Test
    void shouldBuildUnselectedDynamicList() {
        Element<HearingOrdersBundle> hearingOrdersBundle1 = element(HearingOrdersBundle.builder()
            .hearingName(hearing1).orders(newArrayList(agreedCMO(hearing1))).build());

        Element<HearingOrdersBundle> hearingOrdersBundle2 = element(HearingOrdersBundle.builder()
            .hearingName(hearing2).orders(newArrayList(agreedCMO(hearing2))).build());

        List<Element<HearingOrdersBundle>> hearingOrderBundlesDrafts = List.of(
            hearingOrdersBundle1, hearingOrdersBundle2
        );

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(hearingOrderBundlesDrafts)
            .build();

        DynamicList actualDynamicList = underTest.buildUnselectedDynamicList(caseData);
        DynamicList expectedDynamicList = ElementUtils
            .asDynamicList(hearingOrderBundlesDrafts, HearingOrdersBundle::getHearingName);

        assertThat(actualDynamicList).isEqualTo(expectedDynamicList);
    }

    @Test
    void shouldReturnMultiPageDataWhenThereAreMultipleDraftBundlesReadyForApproval() {
        Element<HearingOrdersBundle> hearingOrdersBundle1 = element(HearingOrdersBundle.builder()
            .orders(newArrayList(agreedCMO(hearing1))).build());

        Element<HearingOrdersBundle> hearingOrdersBundle2 = element(HearingOrdersBundle.builder()
            .orders(newArrayList(draftCMO(hearing2),
                element(HearingOrder.builder().type(C21).status(SEND_TO_JUDGE).build()))).build());

        Element<HearingOrdersBundle> hearingOrdersBundle3 = element(HearingOrdersBundle.builder()
            .orders(newArrayList(draftCMO("hearing3"))).build());

        List<Element<HearingOrdersBundle>> hearingOrderBundlesDrafts = List.of(
            hearingOrdersBundle1, hearingOrdersBundle2, hearingOrdersBundle3
        );

        CaseData caseData = CaseData.builder().hearingOrdersBundlesDrafts(hearingOrderBundlesDrafts).build();

        Map<String, Object> expectedData = Map.of(
            "numDraftCMOs", MULTI,
            "cmoToReviewList", DynamicList.builder()
                .value(EMPTY)
                .listItems(List.of(
                    DynamicListElement.builder().code(hearingOrdersBundle1.getId()).build(),
                    DynamicListElement.builder().code(hearingOrdersBundle2.getId()).build()))
                .build());

        assertThat(underTest.getPageDisplayControls(caseData)).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnSinglePageDataWhenThereIsOnlyOneHearingOrdersDraftBundleExists() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);
        Element<HearingOrdersBundle> hearingOrdersBundle = element(HearingOrdersBundle.builder()
            .orders(newArrayList(agreedCMO)).build());

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .build();

        given(draftOrdersReviewDataBuilder.buildDraftOrdersReviewData(hearingOrdersBundle.getValue()))
            .willReturn(BUNDLE_DATA);

        Map<String, Object> actualData = underTest.getPageDisplayControls(caseData);

        HashMap<String, Object> expected = Maps.newHashMap(BUNDLE_DATA);
        expected.put("numDraftCMOs", SINGLE);
        assertThat(actualData).isEqualTo(expected);
    }

    @Test
    void shouldReturnNonePageDataWhenThereAreNoDraftCMOsReadyForApproval() {
        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(emptyList())
            .build();

        Map<String, Object> expectedData = Map.of("numDraftCMOs", NONE);

        assertThat(underTest.getPageDisplayControls(caseData)).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnDraftOrdersDataWhenSelectedHearingOrdersBundleHaveCMOAndDraftOrdersForApproval() {
        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(hearing1,
            newArrayList(agreedCMO(hearing1), buildBlankOrder("Draft C21 order", hearing1)));

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .build();

        given(draftOrdersBundleHearingSelector.getSelectedHearingDraftOrdersBundle(caseData))
            .willReturn(draftOrdersBundle);

        underTest.populateDraftOrdersData(caseData);

        verify(draftOrdersReviewDataBuilder).buildDraftOrdersReviewData(draftOrdersBundle.getValue());
    }

    @Test
    void shouldNotReturnErrorsWhenJudgeApprovesCMO() {
        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(
            hearing1, newArrayList(agreedCMO(hearing1)));

        ReviewDecision reviewDecision = ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build();

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .reviewCMODecision(reviewDecision)
            .build();

        given(draftOrdersBundleHearingSelector.getSelectedHearingDraftOrdersBundle(caseData))
            .willReturn(draftOrdersBundle);

        underTest.validateDraftOrdersReviewDecision(caseData, emptyMap());

        verify(reviewDecisionValidator).validateReviewDecision(reviewDecision, "CMO");
    }

    @ParameterizedTest
    @EnumSource(value = CMOReviewOutcome.class, mode = EnumSource.Mode.MATCH_ALL)
    void shouldReturnTrueWhenJudgeApprovesCMO(CMOReviewOutcome reviewOutcome) {
        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(hearing1,
            newArrayList(agreedCMO(hearing1), buildBlankOrder("Draft C21 order", hearing1)));

        ReviewDecision cmoReviewDecision = ReviewDecision.builder().decision(reviewOutcome).build();
        ReviewDecision c21ReviewDecision = ReviewDecision.builder().decision(JUDGE_REQUESTED_CHANGES).build();

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .reviewCMODecision(cmoReviewDecision)
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().reviewDecision1(c21ReviewDecision).build())
            .build();

        given(draftOrdersBundleHearingSelector.getSelectedHearingDraftOrdersBundle(caseData))
            .willReturn(draftOrdersBundle);

        if (reviewOutcome == JUDGE_REQUESTED_CHANGES) {
            assertThat(underTest.hasApprovedReviewDecision(caseData, emptyMap())).isFalse();
        } else {
            assertThat(underTest.hasApprovedReviewDecision(caseData, emptyMap())).isTrue();
        }
    }

    @ParameterizedTest
    @EnumSource(value = CMOReviewOutcome.class, mode = EnumSource.Mode.MATCH_ALL)
    void shouldReturnTrueWhenJudgeApprovesC21(CMOReviewOutcome reviewOutcome) {
        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(hearing1,
            newArrayList(agreedCMO(hearing1), buildBlankOrder("Draft C21 order", hearing1)));

        ReviewDecision cmoReviewDecision = ReviewDecision.builder().decision(JUDGE_REQUESTED_CHANGES).build();
        ReviewDecision c21ReviewDecision = ReviewDecision.builder().decision(reviewOutcome).build();
        Map<String, Object> reviewDecisionMap = new HashMap<>();
        reviewDecisionMap.put("reviewDecision1", Map.of("decision", c21ReviewDecision));

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .reviewCMODecision(cmoReviewDecision)
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().reviewDecision1(c21ReviewDecision).build())
            .build();

        given(draftOrdersBundleHearingSelector.getSelectedHearingDraftOrdersBundle(caseData))
            .willReturn(draftOrdersBundle);
        given(mapper.convertValue(any(), eq(ReviewDecision.class))).willReturn(c21ReviewDecision);

        if (reviewOutcome == JUDGE_REQUESTED_CHANGES) {
            assertThat(underTest.hasApprovedReviewDecision(caseData, reviewDecisionMap)).isFalse();
        } else {
            assertThat(underTest.hasApprovedReviewDecision(caseData, reviewDecisionMap)).isTrue();
        }
    }

    @Test
    void shouldReturnErrorWhenBundleContainsOnlyDraftOrderAndJudgeDoesNotApprove() {
        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(
            hearing1, newArrayList(buildBlankOrder("title1", hearing1)));

        ReviewDecision reviewDecision = ReviewDecision.builder()
            .decision(JUDGE_REQUESTED_CHANGES)
            .build();

        Map<String, Object> reviewDecisionMap = new HashMap<>();
        reviewDecisionMap.put("reviewDecision1", Map.of("decision", reviewDecision));

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .reviewCMODecision(reviewDecision)
            .build();

        given(mapper.convertValue(anyMap(), eq(ReviewDecision.class))).willReturn(reviewDecision);
        given(draftOrdersBundleHearingSelector.getSelectedHearingDraftOrdersBundle(caseData))
            .willReturn(draftOrdersBundle);

        underTest.validateDraftOrdersReviewDecision(caseData, reviewDecisionMap);

        verify(reviewDecisionValidator).validateReviewDecision(reviewDecision, "draft order 1");
    }

    @Test
    void shouldReturnErrorWhenJudgeDoesNotReviewAnyOrdersInTheSelectedHearingOrdersBundle() {
        Element<HearingOrdersBundle> draftOrdersBundle = buildDraftOrdersBundle(
            hearing1, asList(agreedCMO(hearing1), buildBlankOrder("Draft C21 order", hearing1)));

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(draftOrdersBundle))
            .reviewCMODecision(ReviewDecision.builder().build()) // ReviewDecision.decision not set
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().reviewDecision1(null).build())
            .build();

        given(draftOrdersBundleHearingSelector.getSelectedHearingDraftOrdersBundle(caseData))
            .willReturn(draftOrdersBundle);

        assertThat(underTest.validateDraftOrdersReviewDecision(caseData, emptyMap()))
            .containsOnly("Approve, amend or reject draft orders");
    }

    @ParameterizedTest
    @MethodSource("cmoReviewDecisionData")
    void shouldReturnCMOToSealAndSetCaseStateWhenJudgeApprovesCMOAndServingOthersIsEnabled(
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

        ReviewDecision reviewDecision = ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build();
        CaseData caseData = CaseData.builder()
            .court(Court.builder()
                .code("999")
                .build())
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
            .reviewCMODecision(reviewDecision)
            .hearingDetails(hearingBookings)
            .build();

        String othersNotified = "Other1, Other2";
        List<Element<Other>> others = wrapElements(
            Other.builder().name("Other1").address(Address.builder().postcode("SE1").build()).build(),
            Other.builder().name("Other2").address(Address.builder().postcode("SE2").build()).build());

        HearingOrder expectedCmo = expectedSealedCMO(others, othersNotified);
        Map<String, Object> expectedData = Map.of(
            "sealedCMOs", List.of(element(agreedCMO.getId(), expectedCmo)),
            "ordersToBeSent", List.of(element(agreedCMO.getId(), expectedCmo)),
            "state", expectedState,
            "draftUploadedCMOs", emptyList(),
            "hearingOrdersBundlesDrafts", emptyList(),
            "hearingOrdersBundlesDraftReview", emptyList()
        );

        given(othersService.getSelectedOthers(any(), any(), any())).willReturn(others);
        given(draftOrderService.migrateCmoDraftToOrdersBundles(any(CaseData.class))).willReturn(
            HearingOrdersBundles.builder()
                .agreedCmos(emptyList())
                .draftCmos(emptyList())
                .build()
        );
        given(hearingOrderGenerator.buildSealedHearingOrder(reviewDecision, agreedCMO, others, othersNotified,
            SealType.ENGLISH, caseData.getCourt()))
            .willReturn(element(agreedCMO.getId(), expectedCmo));

        Map<String, Object> actualData = underTest.reviewCMO(caseData, ordersBundleElement);

        assertThat(actualData).containsAllEntriesOf(expectedData);
    }

    @Test
    void shouldRemoveDraftCMOWhenJudgeRequestsChanges() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);

        Element<HearingOrdersBundle> ordersBundleElement = buildDraftOrdersBundle(hearing1, newArrayList(agreedCMO));

        ReviewDecision reviewDecision = ReviewDecision.builder().decision(JUDGE_REQUESTED_CHANGES)
            .changesRequestedByJudge("requested changes text").build();

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
            .reviewCMODecision(reviewDecision)
            .build();

        HearingOrder expectedOrder = agreedCMO.getValue().toBuilder()
            .status(RETURNED)
            .requestedChanges("requested changes text")
            .build();

        when(hearingOrderGenerator.buildRejectedHearingOrder(agreedCMO, reviewDecision.getChangesRequestedByJudge()))
            .thenReturn(element(agreedCMO.getId(), expectedOrder));
        when(draftOrderService.migrateCmoDraftToOrdersBundles(any(CaseData.class)))
            .thenReturn(HearingOrdersBundles.builder()
                .agreedCmos(emptyList())
                .draftCmos(emptyList())
                .build()
            );

        Map<String, Object> expectedData = Map.of(
            "draftUploadedCMOs", emptyList(),
            "hearingOrdersBundlesDrafts", emptyList(),
            "hearingOrdersBundlesDraftReview", emptyList(),
            "ordersToBeSent", List.of(element(agreedCMO.getId(), expectedOrder))
        );

        Map<String, Object> actualData = underTest.reviewCMO(caseData, ordersBundleElement);

        assertThat(actualData).containsAllEntriesOf(expectedData)
            .doesNotContainKeys("selectedCMOs", "state");
    }

    @ParameterizedTest
    @MethodSource("populateNullAndEmptyReviewDecisionValues")
    void shouldNotMakeAnyChangesToCMOWhenTheCMOReviewDecisionIsNotSet() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);
        Element<HearingOrder> blankOrder = buildBlankOrder("order1", hearing1);

        Element<HearingOrdersBundle> ordersBundleElement = buildDraftOrdersBundle(
            hearing1, newArrayList(agreedCMO, blankOrder));

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
            .hearingDetails(newArrayList(
                element(HearingBooking.builder().type(CASE_MANAGEMENT)
                    .caseManagementOrderId(UUID.randomUUID()).build())))
            .build();

        Map<String, Object> actualData = underTest.reviewCMO(caseData, ordersBundleElement);

        assertThat(actualData).isEmpty();
    }

    @Test
    void shouldSealTheDraftOrderAndCreateBlankOrderWhenJudgeApproves() {
        Element<HearingOrder> draftOrder1 = buildBlankOrder("test order1", hearing1);

        Element<HearingOrdersBundle> ordersBundleElement =
            buildDraftOrdersBundle(hearing1, newArrayList(draftOrder1));

        ReviewDecision reviewDecision = ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build();

        Map<String, Object> data = new HashMap<>();
        data.put("reviewDecision1", Map.of("decision", reviewDecision));

        CaseData caseData = CaseData.builder()
            .court(Court.builder()
                    .code("999")
                    .build())
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(newArrayList(draftOrder1))
            .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
            .reviewCMODecision(reviewDecision)
            .orderCollection(newArrayList())
            .build();

        given(mapper.convertValue(anyMap(), eq(ReviewDecision.class))).willReturn(reviewDecision);

        Element<HearingOrder> expectedSealedOrder = element(
            draftOrder1.getId(), draftOrder1.getValue().toBuilder().status(APPROVED).build());

        Element<GeneratedOrder> expectedBlankOrder = element(UUID.randomUUID(),
            GeneratedOrder.builder().type(String.valueOf(C21)).build());

        given(hearingOrderGenerator.buildSealedHearingOrder(reviewDecision, draftOrder1, emptyList(), "",
            SealType.ENGLISH, caseData.getCourt()))
            .willReturn(expectedSealedOrder);
        given(blankOrderGenerator.buildBlankOrder(
            caseData, ordersBundleElement, expectedSealedOrder, emptyList(), ""))
            .willReturn(expectedBlankOrder);

        Map<String, Object> expectedData = Map.of(
            "orderCollection", List.of(expectedBlankOrder),
            "hearingOrdersBundlesDrafts", emptyList(),
            "ordersToBeSent", List.of(expectedSealedOrder)
        );

        underTest.reviewC21Orders(caseData, data, ordersBundleElement);
        assertThat(data).containsAllEntriesOf(expectedData);
    }

    @Test
    void shouldNotCreateBlankOrderWhenJudgeRequestsChanges() {
        Element<HearingOrder> draftOrder1 = buildBlankOrder("test order1", hearing1);

        Element<HearingOrdersBundle> ordersBundleElement =
            buildDraftOrdersBundle(hearing1, newArrayList(draftOrder1));

        ReviewDecision reviewDecision = ReviewDecision.builder().decision(JUDGE_REQUESTED_CHANGES)
            .changesRequestedByJudge("some change").build();

        Map<String, Object> data = new HashMap<>();
        data.put("reviewDecision1", Map.of("decision", JUDGE_REQUESTED_CHANGES));

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(newArrayList(draftOrder1))
            .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().reviewDecision1(reviewDecision).build())
            .orderCollection(newArrayList())
            .build();

        given(mapper.convertValue(anyMap(), eq(ReviewDecision.class))).willReturn(reviewDecision);

        Element<HearingOrder> rejectedOrderToReturn = element(draftOrder1.getId(),
            draftOrder1.getValue().toBuilder().status(RETURNED).requestedChanges("some change").build());

        given(hearingOrderGenerator.buildRejectedHearingOrder(
            draftOrder1, reviewDecision.getChangesRequestedByJudge())).willReturn(rejectedOrderToReturn);

        Map<String, Object> expectedData = Map.of(
            "orderCollection", emptyList(),
            "hearingOrdersBundlesDrafts", emptyList(),
            "ordersToBeSent", List.of(rejectedOrderToReturn)
        );

        underTest.reviewC21Orders(caseData, data, ordersBundleElement);
        assertThat(data).containsAllEntriesOf(expectedData);
        verifyNoInteractions(blankOrderGenerator);
    }

    @Test
    void shouldStoreRefusalReasonsWhenJudgeRejectsDraftOrders() {
        Element<HearingOrder> draftOrder1 = buildBlankOrder("test order1", hearing1);
        Element<HearingOrder> draftOrder2 = buildBlankOrder("test order2", hearing1);
        draftOrder1.getValue().setRequestedChanges("Test requested changes");
        draftOrder2.getValue().setRequestedChanges("Test requested changes");

        Map<String, Object> data = new HashMap<>();
        data.put("ordersToBeSent", List.of(draftOrder1, draftOrder2));

        Map<String, Object> expectedData = Map.of(
            "refusedHearingOrders", List.of(draftOrder1, draftOrder2)
        );

        underTest.updateRejectedHearingOrders(data);
        assertThat(data).containsAllEntriesOf(expectedData);
    }

    @ParameterizedTest
    @MethodSource("populateNullAndEmptyReviewDecisionValues")
    void shouldNotUpdateDataWhenBlankOrderIsNotReviewed(ReviewDecision reviewDecision) {
        Element<HearingOrder> draftOrder1 = buildBlankOrder("test order1", hearing1);

        Element<HearingOrdersBundle> ordersBundleElement =
            buildDraftOrdersBundle(hearing1, newArrayList(draftOrder1));

        Map<String, Object> data = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(newArrayList(draftOrder1))
            .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().reviewDecision1(reviewDecision).build())
            .orderCollection(newArrayList())
            .ordersToBeSent(emptyList())
            .build();

        Map<String, Object> expectedData = Map.of(
            "orderCollection", emptyList(),
            "hearingOrdersBundlesDrafts", List.of(ordersBundleElement)
        );

        underTest.reviewC21Orders(caseData, data, ordersBundleElement);
        assertThat(data).containsAllEntriesOf(expectedData)
            .doesNotContainKey("ordersToBeSent");

        verifyNoInteractions(hearingOrderGenerator, blankOrderGenerator);
    }

    @Test
    void shouldThrowAnExceptionWhenNoUpcomingHearingsAreAvailable() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing2);
        Element<HearingOrdersBundle> ordersBundleElement = buildDraftOrdersBundle(hearing2, newArrayList(agreedCMO));

        CaseData caseData = CaseData.builder()
            .court(Court.builder()
                .code("999")
                .build())
            .state(State.CASE_MANAGEMENT)
            .draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .hearingDetails(emptyList())
            .build();

        given(hearingOrderGenerator.buildSealedHearingOrder(any(), eq(agreedCMO), eq(emptyList()), eq(""),
            eq(SealType.ENGLISH), eq(caseData.getCourt())))
            .willReturn(element(agreedCMO.getId(), agreedCMO.getValue().toBuilder().status(APPROVED).build()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> underTest.reviewCMO(caseData, ordersBundleElement));

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

        assertThat(underTest.getCMOsReadyForApproval(caseData)).isEqualTo(expectedCMOs);
    }

    @Test
    void shouldReturnTheSelectedHearingOrdersBundleFromDynamicList() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);
        Element<HearingOrder> blankOrder = buildBlankOrder("Draft C21 order", hearing2);

        Element<HearingOrdersBundle> selectedHearingBundle = buildDraftOrdersBundle(hearing1, newArrayList(agreedCMO));
        Element<HearingOrdersBundle> hearingBundle2 = buildDraftOrdersBundle(hearing2, newArrayList(blankOrder));

        DynamicList draftBundlesDynamicList = ElementUtils.asDynamicList(
            List.of(selectedHearingBundle, hearingBundle2),
            selectedHearingBundle.getId(), HearingOrdersBundle::getHearingName);

        CaseData caseData = CaseData.builder().draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(List.of(selectedHearingBundle, hearingBundle2))
            .cmoToReviewList(draftBundlesDynamicList)
            .build();

        underTest.getSelectedHearingDraftOrdersBundle(caseData);

        verify(draftOrdersBundleHearingSelector).getSelectedHearingDraftOrdersBundle(caseData);
    }

    @Test
    void shouldGetLatestSealedCMOFromSealedCMOsList() {
        Element<HearingOrder> cmo1 = agreedCMO(hearing1);
        Element<HearingOrder> cmo2 = agreedCMO(hearing2);

        CaseData caseData = CaseData.builder().sealedCMOs(List.of(cmo1, cmo2)).build();

        assertThat(underTest.getLatestSealedCMO(caseData)).isEqualTo(cmo2.getValue());
    }

    @Test
    void shouldThrowExceptionIfSealedCMOListIsEmpty() {
        CaseData caseData = CaseData.builder().sealedCMOs(List.of()).build();
        assertThatExceptionOfType(CMONotFoundException.class).isThrownBy(
            () -> underTest.getLatestSealedCMO(caseData));
    }

    @Nested
    class ConfidentialOrders {
        @Test
        void shouldSealTheConfidentialDraftOrderAndCreateBlankOrderWhenJudgeApproves() {
            Element<HearingOrder> draftOrder1 = buildConfidentialBlankOrder("test order1", hearing1);

            Element<HearingOrdersBundle> ordersBundleElement = buildConfidentialDraftOrdersBundle(hearing1,
                newArrayList(draftOrder1), ConfidentialOrderBundle.SUFFIX_CTSC);

            ReviewDecision reviewDecision = ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build();

            Map<String, Object> data = new HashMap<>();
            data.put("reviewDecision1", Map.of("decision", reviewDecision));

            CaseData caseData = CaseData.builder()
                .court(Court.builder()
                    .code("999")
                    .build())
                .state(State.CASE_MANAGEMENT)
                .draftUploadedCMOs(newArrayList(draftOrder1))
                .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
                .reviewCMODecision(reviewDecision)
                .orderCollection(newArrayList())
                .build();

            given(mapper.convertValue(anyMap(), eq(ReviewDecision.class))).willReturn(reviewDecision);

            Element<HearingOrder> expectedSealedOrder = element(
                draftOrder1.getId(), draftOrder1.getValue().toBuilder().status(APPROVED).build());

            Element<GeneratedOrder> expectedBlankOrder = element(UUID.randomUUID(),
                GeneratedOrder.builder().type(String.valueOf(C21)).build());

            given(hearingOrderGenerator.buildSealedHearingOrder(reviewDecision, draftOrder1, emptyList(), "",
                SealType.ENGLISH, caseData.getCourt()))
                .willReturn(expectedSealedOrder);
            given(blankOrderGenerator.buildBlankOrder(
                caseData, ordersBundleElement, expectedSealedOrder, emptyList(), ""))
                .willReturn(expectedBlankOrder);

            Map<String, Object> expectedData = Map.of(
                "orderCollectionCTSC", List.of(expectedBlankOrder),
                "hearingOrdersBundlesDrafts", emptyList(),
                "ordersToBeSent", List.of(expectedSealedOrder)
            );

            underTest.reviewC21Orders(caseData, data, ordersBundleElement);
            assertThat(data).containsAllEntriesOf(expectedData);
        }


        @Test
        void shouldNotCreateBlankOrderWhenJudgeRequestsChangesOnConfidentialOrder() {
            Element<HearingOrder> draftOrder1 = buildConfidentialBlankOrder("test order1", hearing1);

            Element<HearingOrdersBundle> ordersBundleElement = buildConfidentialDraftOrdersBundle(hearing1,
                newArrayList(draftOrder1), ConfidentialOrderBundle.SUFFIX_CTSC);

            ReviewDecision reviewDecision = ReviewDecision.builder().decision(JUDGE_REQUESTED_CHANGES)
                .changesRequestedByJudge("some change").build();

            Map<String, Object> data = new HashMap<>();
            data.put("reviewDecision1", Map.of("decision", JUDGE_REQUESTED_CHANGES));

            CaseData caseData = CaseData.builder()
                .state(State.CASE_MANAGEMENT)
                .draftUploadedCMOs(newArrayList(draftOrder1))
                .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
                .reviewDraftOrdersData(ReviewDraftOrdersData.builder().reviewDecision1(reviewDecision).build())
                .orderCollection(newArrayList())
                .build();

            given(mapper.convertValue(anyMap(), eq(ReviewDecision.class))).willReturn(reviewDecision);

            Element<HearingOrder> rejectedOrderToReturn = element(draftOrder1.getId(),
                draftOrder1.getValue().toBuilder().status(RETURNED).requestedChanges("some change").build());

            given(hearingOrderGenerator.buildRejectedHearingOrder(
                draftOrder1, reviewDecision.getChangesRequestedByJudge())).willReturn(rejectedOrderToReturn);

            Map<String, Object> expectedData = Map.of(
                "orderCollection", emptyList(),
                "hearingOrdersBundlesDrafts", emptyList(),
                "ordersToBeSent", List.of(rejectedOrderToReturn),
                "refusedHearingOrdersCTSC", List.of(rejectedOrderToReturn)
            );

            underTest.reviewC21Orders(caseData, data, ordersBundleElement);
            assertThat(data).containsAllEntriesOf(expectedData);
            verifyNoInteractions(blankOrderGenerator);
        }

        private static Element<HearingOrdersBundle> buildConfidentialDraftOrdersBundle(
            String hearing, List<Element<HearingOrder>> draftOrders, String suffix) {
            HearingOrdersBundle hearingOrdersBundle = HearingOrdersBundle.builder()
                .orders(new ArrayList<>())
                .hearingName(hearing)
                .judgeTitleAndName("Her Honour Judge Judy").build();
            hearingOrdersBundle.setConfidentialOrdersBySuffix(suffix, draftOrders);
            return element(hearingOrdersBundle);
        }

        private static Element<HearingOrder> buildConfidentialBlankOrder(String title, String hearing) {
            Element<HearingOrder> order = buildBlankOrder(title, hearing);
            order.getValue().setOrderConfidential(order.getValue().getOrder());
            order.getValue().setOrder(null);
            return order;
        }
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

    private static Stream<ReviewDecision> populateNullAndEmptyReviewDecisionValues() {
        return Stream.of(null, ReviewDecision.builder().build(),
            ReviewDecision.builder().decision(REVIEW_LATER).build());
    }

    private HearingOrder expectedSealedCMO(List<Element<Other>> selectedOthers, String othersNotified) {
        return HearingOrder.builder()
            .title(hearing1)
            .order(sealedOrder)
            .lastUploadedOrder(order)
            .hearing(hearing1)
            .dateIssued(TIME.now().toLocalDate())
            .judgeTitleAndName("Her Honour Judge Judy")
            .status(APPROVED)
            .type(AGREED_CMO)
            .others(selectedOthers)
            .othersNotified(othersNotified)
            .build();
    }
}
