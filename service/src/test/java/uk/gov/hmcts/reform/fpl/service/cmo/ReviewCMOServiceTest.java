package uk.gov.hmcts.reform.fpl.service.cmo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
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
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
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
    private final UUID cmoID = UUID.randomUUID();
    private LocalDateTime futureDate;

    @Autowired
    private ReviewCMOService service;

    @Autowired
    private Time time;

    @BeforeEach
    void setUp() {
        futureDate = time.now().plusDays(1);
    }

    @Test
    void shouldBuildDynamicListWithAppropriateElementSelected() {
        Element<HearingOrder> firstCMO = agreedCMO(hearing1);
        List<Element<HearingOrder>> agreedCMOs = List.of(
            firstCMO, agreedCMO(hearing2)
        );
        UUID firstElementId = firstCMO.getId();

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(agreedCMOs)
            //This replicates bug in CCD which sends only String UUID in mid event
            .cmoToReviewList(firstElementId.toString())
            .numDraftCMOs("MULTI")
            .build();

        DynamicList actualDynamicList = service.buildDynamicList(caseData);

        DynamicList expectedDynamicList = ElementUtils
            .asDynamicList(agreedCMOs, firstElementId, HearingOrder::getHearing);

        assertThat(actualDynamicList)
            .isEqualTo(expectedDynamicList);
    }

    @Test
    void shouldBuildUnselectedDynamicList() {
        List<Element<HearingOrder>> agreedCMOs = List.of(
            agreedCMO(hearing1), agreedCMO(hearing2)
        );

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(agreedCMOs)
            .build();

        DynamicList actualDynamicList = service.buildUnselectedDynamicList(caseData);
        DynamicList expectedDynamicList = ElementUtils
            .asDynamicList(agreedCMOs, HearingOrder::getHearing);

        assertThat(actualDynamicList)
            .isEqualTo(expectedDynamicList);
    }

    @Test
    void shouldReturnMultiPageDataWhenThereAreMultipleDraftCMOsReadyForApproval() {
        List<Element<HearingOrder>> agreedCMOs = List.of(agreedCMO(hearing1), agreedCMO(hearing2));
        CaseData caseData = CaseData.builder().draftUploadedCMOs(agreedCMOs).build();

        Map<String, Object> expectedData = Map.of(
            "numDraftCMOs", MULTI,
            "cmoToReviewList", DynamicList.builder()
                .value(EMPTY)
                .listItems(dynamicListItems(agreedCMOs.get(0).getId(), agreedCMOs.get(1).getId())).build());

        assertThat(service.getPageDisplayControls(caseData)).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnSinglePageDataWhenThereIsOneDraftCMOReadyForApproval() {
        CaseData caseData = CaseData.builder().draftUploadedCMOs(List.of(agreedCMO(hearing1))).build();

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
    void shouldReturnCMOToSealWithOriginalDocumentWhenJudgeSelectsSealAndSend() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(List.of(agreedCMO))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .hearingDetails(List.of(element(hearing(agreedCMO.getId())))).build();

        HearingOrder expectedCmo = HearingOrder.builder()
            .order(order)
            .hearing(hearing1)
            .dateIssued(time.now().toLocalDate())
            .judgeTitleAndName("Her Honour Judge Judy")
            .status(APPROVED)
            .build();

        Element<HearingOrder> cmoToSeal = service.getCMOToSeal(caseData);

        assertThat(cmoToSeal.getValue()).isEqualTo(expectedCmo);
        assertThat(cmoToSeal.getId()).isEqualTo(agreedCMO.getId());
    }

    @Test
    void shouldReturnCMOToSealWithJudgeAmendedDocumentWhenJudgeSelectsMakeChanges() {
        Element<HearingOrder> agreedCMO = agreedCMO(hearing1);
        DocumentReference judgeAmendedOrder = testDocumentReference();

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(List.of(agreedCMO))
            .reviewCMODecision(ReviewDecision.builder()
                .decision(JUDGE_AMENDS_DRAFT)
                .judgeAmendedDocument(judgeAmendedOrder)
                .build())
            .hearingDetails(List.of(element(hearing(agreedCMO.getId())))).build();

        HearingOrder expectedCmo = HearingOrder.builder()
            .order(judgeAmendedOrder)
            .hearing(hearing1)
            .dateIssued(time.now().toLocalDate())
            .judgeTitleAndName("Her Honour Judge Judy")
            .status(APPROVED)
            .build();

        Element<HearingOrder> cmoToSeal = service.getCMOToSeal(caseData);

        assertThat(cmoToSeal.getValue()).isEqualTo(expectedCmo);
        assertThat(cmoToSeal.getId()).isEqualTo(agreedCMO.getId());
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
        assertThat(service.getStateBasedOnNextHearing(caseData, cmoID)).isEqualTo(State.CASE_MANAGEMENT);
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
        assertThat(service.getStateBasedOnNextHearing(caseData, cmoID)).isEqualTo(State.FINAL_HEARING);
    }


    @Test
    void shouldReturnCaseManagementStateWhenNextHearingIsNotOfIssueResolutionOrFinalType() {

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
        List<Element<HearingBooking>> hearingBookings = new ArrayList<>();
        CaseData caseData = buildCaseData(SEND_TO_ALL_PARTIES, hearingBookings);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.getStateBasedOnNextHearing(caseData, cmoID));

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
            .order(order)
            .status(status)
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
