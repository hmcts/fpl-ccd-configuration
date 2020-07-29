package uk.gov.hmcts.reform.fpl.service.cmo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement.EMPTY;
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

    @Autowired
    private ReviewCMOService service;

    @Autowired
    private Time time;

    @Test
    void shouldReturnMultiPageDataWhenThereAreMultipleDraftCMOsReadyForApproval() {
        List<Element<CaseManagementOrder>> draftCMOs = List.of(
            draftCMO(hearing1), draftCMO(hearing2));

        Map<String, Object> expectedData = Map.of(
            "numDraftCMOs", MULTI,
            "cmoToReviewList", DynamicList.builder()
                .value(EMPTY)
                .listItems(dynamicListItems(draftCMOs.get(0).getId(), draftCMOs.get(1).getId())).build());

        assertThat(service.handlePageDisplayLogic(draftCMOs)).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnSinglePageDataWhenThereIsOneDraftCMOReadyForApproval() {
        List<Element<CaseManagementOrder>> draftCMOs = List.of(draftCMO(hearing1));

        Map<String, Object> expectedData = Map.of(
            "numDraftCMOs", SINGLE,
            "reviewCMODecision", ReviewDecision.builder()
                .hearing(hearing1)
                .document(order).build());

        assertThat(service.handlePageDisplayLogic(draftCMOs)).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnNonePageDataWhenThereAreNoDraftCMOsReadyForApproval() {
        List<Element<CaseManagementOrder>> draftCMOs = List.of();

        Map<String, Object> expectedData = Map.of(
            "numDraftCMOs", NONE);

        assertThat(service.handlePageDisplayLogic(draftCMOs)).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnCMOsThatAreReadyForApproval() {
        Element<CaseManagementOrder> draftCMO1 = draftCMO(hearing1);
        Element<CaseManagementOrder> draftCMO2 = draftCMO(hearing2);
        Element<CaseManagementOrder> draftCMO3 = element(CaseManagementOrder.builder().status(RETURNED).build());
        Element<CaseManagementOrder> draftCMO4 = element(CaseManagementOrder.builder().status(APPROVED).build());
        List<Element<CaseManagementOrder>> draftCMOs = List.of(draftCMO1, draftCMO2, draftCMO3, draftCMO4);

        List<Element<CaseManagementOrder>> expectedCMOs = List.of(draftCMO1, draftCMO2);
        assertThat(service.getCMOsReadyForApproval(draftCMOs)).isEqualTo(expectedCMOs);
    }

    @Test
    void shouldReturnCMOToSealWithOriginalDocumentWhenJudgeSelectsSealAndSend() {
        Element<CaseManagementOrder> draftCMO = draftCMO(hearing1);

        CaseData caseData = CaseData.builder()
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .hearingDetails(List.of(element(hearing(draftCMO.getId())))).build();

        CaseManagementOrder expectedCmo = CaseManagementOrder.builder()
            .order(order)
            .hearing(hearing1)
            .dateIssued(time.now().toLocalDate())
            .judgeTitleAndName("Her Honour Judge Judy")
            .build();

        assertThat(service.getCMOToSeal(caseData, draftCMO).getValue()).isEqualTo(expectedCmo);
    }

    @Test
    void shouldReturnCMOToSealWithJudgeAmendedDocumentWhenJudgeSelectsMakeChanges() {
        Element<CaseManagementOrder> draftCMO = draftCMO(hearing1);
        DocumentReference judgeAmendedOrder = testDocumentReference();

        CaseData caseData = CaseData.builder()
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
            .build();

        assertThat(service.getCMOToSeal(caseData, draftCMO).getValue()).isEqualTo(expectedCmo);
    }

    @Test
    void shouldThrowExceptionWhenCMOToSealCannotBeLinkedToHearing() {
        Element<CaseManagementOrder> draftCMO = draftCMO(hearing1);

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearing(UUID.randomUUID())))).build();

        assertThatExceptionOfType(NoHearingBookingException.class).isThrownBy(
            () -> service.getCMOToSeal(caseData, draftCMO));
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

        List<Element<CaseManagementOrder>> cmos = List.of(cmo1, cmo2);

        assertThat(service.getLatestSealedCMO(cmos)).isEqualTo(cmo2.getValue());
    }

    @Test
    void shouldThrowExceptionIfSealedCMOListIsEmpty() {
        assertThatExceptionOfType(CMONotFoundException.class).isThrownBy(
            () -> service.getLatestSealedCMO(List.of()));
    }

    private Element<CaseManagementOrder> draftCMO(String hearing) {
        return element(CaseManagementOrder.builder()
            .hearing(hearing)
            .order(order)
            .status(SEND_TO_JUDGE).build());
    }

    private List<DynamicListElement> dynamicListItems(UUID uuid1, UUID uuid2) {
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

    private HearingBooking hearing(UUID cmoId) {
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
}
