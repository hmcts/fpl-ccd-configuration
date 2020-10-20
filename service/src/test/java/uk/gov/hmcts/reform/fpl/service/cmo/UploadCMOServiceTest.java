package uk.gov.hmcts.reform.fpl.service.cmo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.CMOType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.UploadCMOEventData;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FURTHER_CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, FixedTimeConfiguration.class, UploadCMOService.class
})
class UploadCMOServiceTest {

    private static final DocumentReference DOCUMENT = DocumentReference.builder().build();
    @Autowired
    private UploadCMOService service;

    @Autowired
    private Time time;

    @Test
    void shouldReturnMultiPageDataWhenThereAreMultipleHearings() {
        List<Element<HearingBooking>> hearings = hearings();

        UploadCMOEventData initialPageData = service.getInitialPageData(hearings, List.of());

        UploadCMOEventData expectedEventData = UploadCMOEventData.builder()
            .pastHearingsForCMO(dynamicList(hearings.get(0).getId(),
                hearings.get(1).getId(),
                hearings.get(2).getId()))
            .numHearingsWithoutCMO(UploadCMOEventData.NumberOfHearingsOptions.MULTI)
            .build();

        assertThat(initialPageData).isEqualTo(expectedEventData);
    }

    @Test
    void shouldReturnMultiPageDataWhenThereAreMultipleHearingsWithSomeHearingsAlreadyMappedToCMOs() {
        List<Element<HearingBooking>> hearings = new ArrayList<>(hearings());

        Element<CaseManagementOrder> cmo = element(CaseManagementOrder.builder().status(SEND_TO_JUDGE).build());
        Element<HearingBooking> hearing = element(
            hearing(CASE_MANAGEMENT, LocalDateTime.of(2020, 1, 15, 0, 0), cmo.getId())
        );

        hearings.add(hearing);

        UploadCMOEventData initialPageData = service.getInitialPageData(hearings, List.of(cmo));

        UploadCMOEventData expectedEventData = UploadCMOEventData.builder()
            .pastHearingsForCMO(dynamicList(hearings.get(0).getId(),
                hearings.get(1).getId(),
                hearings.get(2).getId()))
            .numHearingsWithoutCMO(UploadCMOEventData.NumberOfHearingsOptions.MULTI)
            .multiHearingsWithCMOs("Case management hearing, 15 January 2020")
            .showHearingsMultiTextArea(YesNo.YES)
            .build();

        assertThat(initialPageData).isEqualTo(expectedEventData);
    }

    @Test
    void shouldReturnSinglePageDataWhenThereIsOneRemainingHearing() {
        List<Element<HearingBooking>> hearings = List.of(element(
            hearing(CASE_MANAGEMENT, LocalDateTime.of(2020, 2, 1, 0, 0))
        ));

        UploadCMOEventData initialPageData = service.getInitialPageData(hearings, List.of());

        UploadCMOEventData expectedEventData = UploadCMOEventData.builder()
            .numHearingsWithoutCMO(UploadCMOEventData.NumberOfHearingsOptions.SINGLE)
            .cmoHearingInfo("Send agreed CMO for Case management hearing, 1 February 2020."
                + "\nThis must have been discussed by all parties at the hearing.")
            .cmoJudgeInfo("His Honour Judge Dredd")
            .build();

        assertThat(initialPageData).isEqualTo(expectedEventData);
    }

    @Test
    void shouldReturnSinglePageDataWhenThereIsOneRemainingHearingWithSomeHearingsAlreadyMappedToCMOs() {
        Element<CaseManagementOrder> cmo = element(CaseManagementOrder.builder().status(SEND_TO_JUDGE).build());
        List<Element<HearingBooking>> hearings = List.of(
            element(hearing(CASE_MANAGEMENT, LocalDateTime.of(2020, 2, 1, 0, 0))),
            element(hearing(CASE_MANAGEMENT, LocalDateTime.of(2020, 2, 2, 0, 0), cmo.getId()))
        );

        UploadCMOEventData initialPageData = service.getInitialPageData(hearings, List.of(cmo));

        UploadCMOEventData expectedEventData = UploadCMOEventData.builder()
            .numHearingsWithoutCMO(UploadCMOEventData.NumberOfHearingsOptions.SINGLE)
            .cmoHearingInfo("Send agreed CMO for Case management hearing, 1 February 2020."
                + "\nThis must have been discussed by all parties at the hearing.")
            .cmoJudgeInfo("His Honour Judge Dredd")
            .singleHearingWithCMO("Case management hearing, 2 February 2020")
            .showHearingsSingleTextArea(YesNo.YES)
            .build();

        assertThat(initialPageData).isEqualTo(expectedEventData);
    }

    @Test
    void shouldReturnPageShowHideFieldOnlyWhenThereAreNoRemainingHearingsWithoutCmoMappings() {
        UploadCMOEventData initialPageData = service.getInitialPageData(List.of(), List.of());

        UploadCMOEventData expectedEventData = UploadCMOEventData.builder()
            .numHearingsWithoutCMO(UploadCMOEventData.NumberOfHearingsOptions.NONE)
            .build();

        assertThat(initialPageData).isEqualTo(expectedEventData);
    }

    @Test
    void shouldNotIncludeReturnedHearingsInCMOTextArea() {
        List<Element<HearingBooking>> hearings = new ArrayList<>(hearings());

        Element<CaseManagementOrder> cmo = element(CaseManagementOrder.builder().status(SEND_TO_JUDGE).build());
        Element<CaseManagementOrder> returnedCMO = element(CaseManagementOrder.builder().status(RETURNED).build());
        List<Element<HearingBooking>> additionalHearings = List.of(
            element(hearing(
                CASE_MANAGEMENT, LocalDateTime.of(2020, 1, 15, 0, 0), cmo.getId())
            ),
            element(hearing(
                CASE_MANAGEMENT, LocalDateTime.of(2020, 1, 16, 0, 0), returnedCMO.getId())
            )
        );

        hearings.addAll(additionalHearings);

        UploadCMOEventData initialPageData = service.getInitialPageData(hearings, List.of(cmo, returnedCMO));

        DynamicListElement listElement = DynamicListElement.builder()
            .code(additionalHearings.get(1).getId())
            .label("Case management hearing, 16 January 2020")
            .build();

        DynamicList dynamicList = dynamicList(
            hearings.get(0).getId(),
            hearings.get(1).getId(),
            hearings.get(2).getId(),
            listElement
        );

        UploadCMOEventData expectedEventData = UploadCMOEventData.builder()
            .pastHearingsForCMO(dynamicList)
            .numHearingsWithoutCMO(UploadCMOEventData.NumberOfHearingsOptions.MULTI)
            .multiHearingsWithCMOs("Case management hearing, 15 January 2020")
            .showHearingsMultiTextArea(YesNo.YES)
            .build();

        assertThat(initialPageData).isEqualTo(expectedEventData);
    }

    @Test
    void shouldGenerateHearingAndJudgeLabelForSelectedHearing() {
        List<Element<HearingBooking>> hearings = hearings();

        DynamicList dynamicList = dynamicList(
            hearings.get(0).getId(),
            hearings.get(1).getId(),
            hearings.get(2).getId(),
            true
        );

        UploadCMOEventData preparedData = service.prepareJudgeAndHearingDetails(dynamicList, hearings, List.of());

        UploadCMOEventData expectedData = UploadCMOEventData.builder()
            .cmoHearingInfo("Case management hearing, 2 March 2020")
            .cmoJudgeInfo("His Honour Judge Dredd")
            .build();

        assertThat(preparedData).isEqualTo(expectedData);
    }

    @Test
    void shouldReconstructDynamicListFromMalformedData() {
        List<Element<HearingBooking>> hearings = hearings();
        String malformedData = hearings.get(0).getId().toString();

        UploadCMOEventData preparedData = service.prepareJudgeAndHearingDetails(malformedData, hearings, List.of());

        DynamicList dynamicList = dynamicList(
            hearings.get(0).getId(),
            hearings.get(1).getId(),
            hearings.get(2).getId(),
            true
        );

        assertThat(preparedData).extracting(UploadCMOEventData::getPastHearingsForCMO).isEqualTo(dynamicList);
    }

    @Test
    void shouldNotReconstructDynamicListIfNotMalformed() {
        List<Element<HearingBooking>> hearings = hearings();

        DynamicList dynamicList = dynamicList(
            hearings.get(0).getId(),
            hearings.get(1).getId(),
            hearings.get(2).getId(),
            true
        );

        UploadCMOEventData preparedData = service.prepareJudgeAndHearingDetails(dynamicList, hearings, List.of());

        assertThat(preparedData).extracting(UploadCMOEventData::getPastHearingsForCMO).isNull();
    }

    @Test
    void shouldAddNewCMOToListAndUpdateHearingIfCMOWasNotAlreadyInList() {
        List<Element<HearingBooking>> hearings = hearings();
        DynamicList dynamicList = dynamicList(
            hearings.get(0).getId(),
            hearings.get(1).getId(),
            hearings.get(2).getId(),
            true
        );
        List<Element<CaseManagementOrder>> unsealedOrders = new ArrayList<>();

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .pastHearingsForCMO(dynamicList)
            .uploadedCaseManagementOrder(DOCUMENT)
            .build();

        service.updateHearingsAndOrders(eventData, hearings, unsealedOrders, List.of());

        CaseManagementOrder expectedOrder = CaseManagementOrder.builder()
            .status(SEND_TO_JUDGE)
            .dateSent(time.now().toLocalDate())
            .order(DOCUMENT)
            .hearing("Case management hearing, 2 March 2020")
            .judgeTitleAndName("His Honour Judge Dredd")
            .build();

        assertThat(unsealedOrders).isNotEmpty()
            .first()
            .extracting(Element::getValue)
            .isEqualTo(expectedOrder);

        assertThat(hearings).hasSize(3)
            .first()
            .extracting(hearing -> hearing.getValue().getCaseManagementOrderId())
            .isEqualTo(unsealedOrders.get(0).getId());
    }

    @Test
    void shouldUpdateExistingCMOWithNewOrderAndChangeStatus() {
        List<Element<HearingBooking>> hearings = hearings();
        List<Element<CaseManagementOrder>> unsealedOrders = new ArrayList<>();
        Element<CaseManagementOrder> oldOrder = element(CaseManagementOrder.builder().status(RETURNED).build());

        unsealedOrders.add(oldOrder);
        unsealedOrders.add(element(CaseManagementOrder.builder().build()));

        hearings.get(0).getValue().setCaseManagementOrderId(unsealedOrders.get(0).getId());

        DynamicList dynamicList = dynamicList(
            hearings.get(0).getId(),
            hearings.get(1).getId(),
            hearings.get(2).getId(),
            true
        );

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .cmoUploadType(CMOType.AGREED)
            .pastHearingsForCMO(dynamicList)
            .uploadedCaseManagementOrder(DOCUMENT)
            .build();

        service.updateHearingsAndOrders(eventData, hearings, unsealedOrders, List.of());

        CaseManagementOrder expectedOrder = CaseManagementOrder.builder()
            .status(SEND_TO_JUDGE)
            .dateSent(time.now().toLocalDate())
            .order(DOCUMENT)
            .hearing("Case management hearing, 2 March 2020")
            .judgeTitleAndName("His Honour Judge Dredd")
            .build();

        assertThat(unsealedOrders).hasSize(2)
            .first()
            .extracting(Element::getValue)
            .isNotEqualTo(oldOrder.getValue())
            .isEqualTo(expectedOrder);

        assertThat(hearings).hasSize(3)
            .first()
            .extracting(hearing -> hearing.getValue().getCaseManagementOrderId())
            .isNotEqualTo(oldOrder.getId())
            .isEqualTo(unsealedOrders.get(0).getId());
    }

    @Test
    void shouldReturnTrueIfThereIsADifferenceBetweenTheCmoLists() {
        List<Element<CaseManagementOrder>> current = wrapElements(CaseManagementOrder.builder().build());
        List<Element<CaseManagementOrder>> before = List.of();

        // Duplicate to not effect passed list
        List<Element<CaseManagementOrder>> current1 = new ArrayList<>(current);

        current1.removeAll(before);

        assertThat(!current1.isEmpty()).isTrue();
    }

    private DynamicList dynamicList(UUID uuid1, UUID uuid2, UUID uuid3, DynamicListElement... additional) {
        return dynamicList(uuid1, uuid2, uuid3, false, additional);
    }

    private DynamicList dynamicList(UUID uuid1, UUID uuid2, UUID uuid3, boolean withValue,
                                    DynamicListElement... additional) {
        DynamicListElement value;
        if (withValue) {
            value = DynamicListElement.builder()
                .code(uuid1)
                .label("Case management hearing, 2 March 2020")
                .build();
        } else {
            value = DynamicListElement.EMPTY;
        }

        List<DynamicListElement> listItems = new ArrayList<>(List.of(
            DynamicListElement.builder()
                .code(uuid1)
                .label("Case management hearing, 2 March 2020")
                .build(),
            DynamicListElement.builder()
                .code(uuid2)
                .label("Further case management hearing, 7 March 2020")
                .build(),
            DynamicListElement.builder()
                .code(uuid3)
                .label("Final hearing, 12 March 2020")
                .build()
        ));

        listItems.addAll(Arrays.asList(additional));

        return DynamicList.builder()
            .value(value)
            .listItems(listItems)
            .build();
    }

    private List<Element<HearingBooking>> hearings() {
        LocalTime time = LocalTime.now();
        return List.of(
            element(hearing(CASE_MANAGEMENT, LocalDateTime.of(LocalDate.of(2020, 3, 2), time))),
            element(hearing(FURTHER_CASE_MANAGEMENT, LocalDateTime.of(LocalDate.of(2020, 3, 7), time))),
            element(hearing(FINAL, LocalDateTime.of(LocalDate.of(2020, 3, 12), time)))
        );
    }

    private HearingBooking hearing(HearingType type, LocalDateTime startDate) {
        return hearing(type, startDate, null);
    }

    private HearingBooking hearing(HearingType type, LocalDateTime startDate, UUID cmoId) {
        return HearingBooking.builder()
            .type(type)
            .startDate(startDate)
            .caseManagementOrderId(cmoId)
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor())
            .build();
    }

    private JudgeAndLegalAdvisor judgeAndLegalAdvisor() {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Dredd")
            .build();
    }
}
