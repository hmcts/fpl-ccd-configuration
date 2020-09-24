package uk.gov.hmcts.reform.fpl.service.cmo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
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
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FURTHER_CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement.EMPTY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, FixedTimeConfiguration.class, ReviewCMOService.class})
class ReviewCMOServiceTest {

    private static final String SINGLE = "SINGLE";
    private static final String MULTI = "MULTI";
    private static final String NONE = "NONE";
    private static final String hearing1 = "Case management hearing, 2 March 2020";
    private static final String hearing2 = "Test hearing, 15 October 2020";
    private static final DocumentReference order = testDocumentReference();
    private UUID cmoID = UUID.randomUUID();
    private LocalDateTime futureDate;

    @Autowired
    private ReviewCMOService service;

    @Autowired
    private Time time;

    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        futureDate = time.now().plusDays(1);
    }

    @Test
    void shouldBuildDynamicListWithAppropriateElementSelected() {
        Element<CaseManagementOrder> firstCMO = draftCMO(hearing1);
        List<Element<CaseManagementOrder>> draftCMOs = List.of(
            firstCMO, draftCMO(hearing2)
        );
        UUID firstElementId = firstCMO.getId();

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(draftCMOs)
            //This replicates bug in CCD which sends only String UUID in mid event
            .cmoToReviewList(firstElementId.toString())
            .numDraftCMOs("MULTI")
            .build();

        DynamicList actualDynamicList = service.buildDynamicList(caseData);

        DynamicList expectedDynamicList = ElementUtils
            .asDynamicList(draftCMOs, firstElementId, CaseManagementOrder::getHearing);

        assertThat(actualDynamicList)
            .isEqualTo(expectedDynamicList);
    }

    @Test
    void shouldBuildUnselectedDynamicList() {
        List<Element<CaseManagementOrder>> draftCMOs = List.of(
            draftCMO(hearing1), draftCMO(hearing2)
        );

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(draftCMOs)
            .build();

        DynamicList actualDynamicList = service.buildUnselectedDynamicList(caseData);
        DynamicList expectedDynamicList = ElementUtils
            .asDynamicList(draftCMOs, CaseManagementOrder::getHearing);

        assertThat(actualDynamicList)
            .isEqualTo(expectedDynamicList);
    }

    @Test
    void shouldReturnMultiPageDataWhenThereAreMultipleDraftCMOsReadyForApproval() {
        List<Element<CaseManagementOrder>> draftCMOs = List.of(draftCMO(hearing1), draftCMO(hearing2));
        CaseData caseData = CaseData.builder().draftUploadedCMOs(draftCMOs).build();

        Map<String, Object> expectedData = Map.of(
            "numDraftCMOs", MULTI,
            "cmoToReviewList", DynamicList.builder()
                .value(EMPTY)
                .listItems(dynamicListItems(draftCMOs.get(0).getId(), draftCMOs.get(1).getId())).build());

        assertThat(service.getPageDisplayControls(caseData)).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnSinglePageDataWhenThereIsOneDraftCMOReadyForApproval() {
        CaseData caseData = CaseData.builder().draftUploadedCMOs(List.of(draftCMO(hearing1))).build();

        Map<String, Object> expectedData = Map.of(
            "numDraftCMOs", SINGLE,
            "reviewCMODecision", ReviewDecision.builder()
                .hearing(hearing1)
                .document(order).build());

        assertThat(service.getPageDisplayControls(caseData)).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnNonePageDataWhenThereAreNoDraftCMOsReadyForApproval() {
        CaseData caseData = CaseData.builder().draftUploadedCMOs(List.of()).build();

        Map<String, Object> expectedData = Map.of(
            "numDraftCMOs", NONE);

        assertThat(service.getPageDisplayControls(caseData)).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnCMOsThatAreReadyForApproval() {
        Element<CaseManagementOrder> draftCMO1 = element(CaseManagementOrder.builder().status(SEND_TO_JUDGE).build());
        Element<CaseManagementOrder> draftCMO2 = element(CaseManagementOrder.builder().status(SEND_TO_JUDGE).build());
        Element<CaseManagementOrder> draftCMO3 = element(CaseManagementOrder.builder().status(RETURNED).build());
        Element<CaseManagementOrder> draftCMO4 = element(CaseManagementOrder.builder().status(APPROVED).build());

        List<Element<CaseManagementOrder>> draftCMOs = List.of(draftCMO1, draftCMO2, draftCMO3, draftCMO4);
        CaseData caseData = CaseData.builder().draftUploadedCMOs(draftCMOs).build();

        List<Element<CaseManagementOrder>> expectedCMOs = List.of(draftCMO1, draftCMO2);

        assertThat(service.getCMOsReadyForApproval(caseData)).isEqualTo(expectedCMOs);
    }

    @Test
    void shouldReturnCMOToSealWithOriginalDocumentWhenJudgeSelectsSealAndSend() {
        Element<CaseManagementOrder> draftCMO = draftCMO(hearing1);

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(List.of(draftCMO))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .hearingDetails(List.of(element(hearing(draftCMO.getId())))).build();

        CaseManagementOrder expectedCmo = CaseManagementOrder.builder()
            .order(order)
            .hearing(hearing1)
            .dateIssued(time.now().toLocalDate())
            .judgeTitleAndName("Her Honour Judge Judy")
            .status(APPROVED)
            .build();

        assertThat(service.getCMOToSeal(caseData).getValue()).isEqualTo(expectedCmo);
    }

    @Test
    void shouldReturnCMOToSealWithJudgeAmendedDocumentWhenJudgeSelectsMakeChanges() {
        Element<CaseManagementOrder> draftCMO = draftCMO(hearing1);
        DocumentReference judgeAmendedOrder = testDocumentReference();

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(List.of(draftCMO))
            .reviewCMODecision(ReviewDecision.builder()
                .decision(JUDGE_AMENDS_DRAFT)
                .judgeAmendedDocument(judgeAmendedOrder)
                .build())
            .hearingDetails(List.of(element(hearing(draftCMO.getId())))).build();

        CaseManagementOrder expectedCmo = CaseManagementOrder.builder()
            .order(judgeAmendedOrder)
            .hearing(hearing1)
            .dateIssued(time.now().toLocalDate())
            .judgeTitleAndName("Her Honour Judge Judy")
            .status(APPROVED)
            .build();

        assertThat(service.getCMOToSeal(caseData).getValue()).isEqualTo(expectedCmo);
    }

    @Test
    void shouldReturnCMOThatWasSelectedFromDynamicListWhenMultipleCMOsExist() {
        Element<CaseManagementOrder> draftCMO1 = draftCMO(hearing1);
        Element<CaseManagementOrder> draftCMO2 = draftCMO(hearing1);

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(List.of(draftCMO1, draftCMO2))
            .cmoToReviewList(DynamicList.builder()
                .value(DynamicListElement.builder().code(draftCMO1.getId()).build())
                .listItems(dynamicListItems(draftCMO1.getId(), draftCMO2.getId())).build())
            .numDraftCMOs(MULTI)
            .build();

        assertThat(service.getSelectedCMO(caseData)).isEqualTo(draftCMO1);
    }

    @Test
    void shouldReturnCMOFromDraftListWhenOnlyOneCMOExists() {
        Element<CaseManagementOrder> draftCMO = draftCMO(hearing1);
        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(List.of(draftCMO))
            .numDraftCMOs(SINGLE)
            .build();

        assertThat(service.getSelectedCMO(caseData)).isEqualTo(draftCMO);
    }

    @Test
    void shouldGetLatestSealedCMOFromSealedCMOsList() {
        Element<CaseManagementOrder> cmo1 = draftCMO(hearing1);
        Element<CaseManagementOrder> cmo2 = draftCMO(hearing2);

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
    void shouldReturnCaseManagementStateWhenNextHearingIsOfIssueResolutionTypeAndFeatureToggleIsToggledOn() {
        given(featureToggleService.isNewCaseStateModelEnabled()).willReturn(true);

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6), FINAL, cmoID)),
            element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3), CASE_MANAGEMENT,
                UUID.randomUUID())),
            element(createHearingBooking(futureDate.plusDays(6), futureDate.plusDays(7), ISSUE_RESOLUTION,
                UUID.randomUUID())),
            element(createHearingBooking(futureDate, futureDate.plusDays(1), ISSUE_RESOLUTION, UUID.randomUUID())));

        CaseData caseData = buildCaseData(SEND_TO_ALL_PARTIES, hearingBookings);
        assertThat(service.getStateBasedOnNextHearing(caseData, cmoID)).isEqualTo(State.CASE_MANAGEMENT);
    }

    @Test
    void shouldReturnFinalHearingStateWhenNextHearingIsOfFinalTypeAndFeatureToggleIsToggledOn() {
        given(featureToggleService.isNewCaseStateModelEnabled()).willReturn(true);

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6), FINAL, cmoID)),
            element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3), CASE_MANAGEMENT,
                UUID.randomUUID())),
            element(createHearingBooking(futureDate.plusDays(6), futureDate.plusDays(7), FINAL,
                UUID.randomUUID())),
            element(createHearingBooking(futureDate, futureDate.plusDays(1), ISSUE_RESOLUTION, UUID.randomUUID())));

        CaseData caseData = buildCaseData(SEND_TO_ALL_PARTIES, hearingBookings);
        assertThat(service.getStateBasedOnNextHearing(caseData, cmoID)).isEqualTo(State.FINAL_HEARING);
    }

    @Test
    void shouldReturnCaseManagementStateWhenNextHearingIsOfIssueResolutionTypeAndFeatureToggleIsToggledOff() {
        given(featureToggleService.isNewCaseStateModelEnabled()).willReturn(false);

        CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .build();

        assertThat(service.getStateBasedOnNextHearing(caseData, cmoID)).isEqualTo(caseData.getState());
    }

    @Test
    void shouldReturnCaseManagementStateWhenNextHearingIsNotOfIssueResolutionOrFinalType() {
        given(featureToggleService.isNewCaseStateModelEnabled()).willReturn(true);

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6), FURTHER_CASE_MANAGEMENT,
                cmoID)),
            element(createHearingBooking(futureDate.plusDays(6), futureDate.plusDays(7), CASE_MANAGEMENT,
                UUID.randomUUID())));

        CaseData caseData = buildCaseData(SEND_TO_ALL_PARTIES, hearingBookings);
        assertThat(service.getStateBasedOnNextHearing(caseData, cmoID)).isEqualTo(State.CASE_MANAGEMENT);
    }

    @Test
    void shouldReturnCaseManagementStateWhenNextReviewDecisionIsNotSendToAllParties() {
        given(featureToggleService.isNewCaseStateModelEnabled()).willReturn(true);

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6), FINAL, cmoID)),
            element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3), CASE_MANAGEMENT,
                UUID.randomUUID())),
            element(createHearingBooking(futureDate.plusDays(6), futureDate.plusDays(7), ISSUE_RESOLUTION,
                UUID.randomUUID())),
            element(createHearingBooking(futureDate, futureDate.plusDays(1), ISSUE_RESOLUTION, UUID.randomUUID())));

        CaseData caseData = buildCaseData(JUDGE_AMENDS_DRAFT, hearingBookings);
        assertThat(service.getStateBasedOnNextHearing(caseData, cmoID)).isEqualTo(State.CASE_MANAGEMENT);
    }

    @Test
    void shouldThrowAnExceptionWhenNoUpcomingHearingsAreAvailable() {
        given(featureToggleService.isNewCaseStateModelEnabled()).willReturn(true);
        List<Element<HearingBooking>> hearingBookings = new ArrayList<>();
        CaseData caseData = buildCaseData(SEND_TO_ALL_PARTIES, hearingBookings);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.getStateBasedOnNextHearing(caseData, cmoID));

        assertThat(exception).hasMessageContaining("Failed to find hearing matching cmo id", cmoID);
    }

    private static Element<CaseManagementOrder> draftCMO(String hearing) {
        return element(CaseManagementOrder.builder()
            .hearing(hearing)
            .order(order)
            .status(SEND_TO_JUDGE)
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
            .reviewCMODecision(ReviewDecision.builder().decision(cmoReviewOutcome).build())
            .hearingDetails(hearingDetails)
            .build();
    }
}
