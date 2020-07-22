package uk.gov.hmcts.reform.fpl.service.cmo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FURTHER_CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, UploadCMOService.class})
class UploadCMOServiceTest {

    @Autowired
    private UploadCMOService service;

    @Test
    void shouldReturnEmptyListWhenThereAreNoHearingsWithoutCMOs() {
        List<Element<HearingBooking>> hearings = wrapElements(
            hearing(randomUUID()),
            hearing(randomUUID()),
            hearing(randomUUID())
        );

        List<Element<HearingBooking>> filtered = service.getHearingsWithoutCMO(hearings, List.of());

        assertThat(filtered).isEmpty();
    }

    @Test
    void shouldReturnPopulatedListWhenHearingsHaveNoCMORelationship() {
        List<Element<HearingBooking>> hearings = wrapElements(hearing(randomUUID()), hearing(), hearing(randomUUID()));

        List<Element<HearingBooking>> filtered = service.getHearingsWithoutCMO(hearings, List.of());

        Element<HearingBooking> expected = hearings.get(1);

        assertThat(filtered).containsExactly(expected);
    }

    @Test
    void shouldIncludeHearingsAssociatedToReturnedCMOsInReturnedList() {
        UUID cmoId = randomUUID();
        List<Element<HearingBooking>> hearings = wrapElements(
            hearing(randomUUID()),
            hearing(cmoId),
            hearing()
        );

        Element<CaseManagementOrder> cmo = element(cmoId, CaseManagementOrder.builder().status(RETURNED).build());

        List<Element<HearingBooking>> filtered = service.getHearingsWithoutCMO(hearings, List.of(cmo));

        assertThat(filtered).containsExactly(hearings.get(1), hearings.get(2));
    }

    @Test
    void shouldReturnEmptyListWhenEmptyListPassed() {
        assertThat(service.getHearingsWithoutCMO(List.of(), List.of())).isEmpty();
    }

    @Test
    void shouldUpdateHearingWithCMOId() {
        List<Element<HearingBooking>> hearings = List.of(element(hearing()));
        UUID selectedId = hearings.get(0).getId();
        Element<CaseManagementOrder> cmo = element(CaseManagementOrder.builder().build());

        UUID uuid = service.mapToHearing(selectedId, hearings, cmo);

        assertThat(uuid).isNull();
        assertThat(hearings.get(0).getValue().getCaseManagementOrderId()).isEqualTo(cmo.getId());
    }

    @Test
    void shouldReturnOldIdWhenNewCMOIsMapped() {
        UUID oldId = randomUUID();
        List<Element<HearingBooking>> hearings = List.of(element(hearing(oldId)));
        UUID selectedId = hearings.get(0).getId();
        Element<CaseManagementOrder> cmo = element(CaseManagementOrder.builder().build());

        UUID uuid = service.mapToHearing(selectedId, hearings, cmo);

        assertThat(uuid).isEqualTo(oldId);
    }

    @Test
    void shouldGetHearingWithSameIdAsPassed() {
        List<Element<HearingBooking>> hearings = List.of(element(hearing()), element(hearing()), element(hearing()));
        UUID selected = hearings.get(2).getId();

        HearingBooking selectedHearing = service.getSelectedHearing(selected, hearings);

        assertThat(selectedHearing).isEqualTo(hearings.get(2).getValue());
    }

    @Test
    void shouldThrowErrorWhenNoHearingMatchesTheIdPassed() {
        List<Element<HearingBooking>> hearings = List.of(element(hearing()), element(hearing()), element(hearing()));
        UUID selected = randomUUID();

        assertThatThrownBy(() -> service.getSelectedHearing(selected, hearings))
            .isInstanceOf(HearingNotFoundException.class)
            .hasMessage("No hearing found with id: %s", selected);
    }

    @Test
    void shouldExtractIdWhenStringIsPassedAsDynamicList() {
        UUID uuid = randomUUID();
        String id = uuid.toString();

        UUID selectedId = service.getSelectedHearingId(id, List.of());

        assertThat(selectedId).isEqualTo(uuid);
    }

    @Test
    void shouldExtractSelectedIdWhenDynamicListMapIsPassed() {
        UUID uuid = randomUUID();
        Map<String, Object> dynamicList = Map.of(
            "value", Map.of(
                "code", uuid,
                "label", "random uuid above"
            ),
            "list_items", List.of()
        );

        UUID selected = service.getSelectedHearingId(dynamicList, List.of());

        assertThat(selected).isEqualTo(uuid);
    }

    @Test
    void shouldReturnIdOfFirstValueInListWhenOnlyOneElementInList() {
        List<Element<HearingBooking>> hearings = wrapElements(hearing());

        UUID selected = service.getSelectedHearingId(null, hearings);

        assertThat(selected).isEqualTo(hearings.get(0).getId());
    }

    @Test
    void shouldBuildADynamicListWithNoSelectedValue() {
        List<Element<HearingBooking>> hearings = hearings();

        DynamicList dynamicList = service.buildDynamicList(hearings);

        DynamicList expected = dynamicList(hearings.get(0).getId(), hearings.get(1).getId(), hearings.get(2).getId());

        assertThat(dynamicList).isEqualTo(expected);
    }

    @Test
    void shouldBuildADynamicListWithASelectedValue() {
        List<Element<HearingBooking>> hearings = hearings();

        DynamicList dynamicList = service.buildDynamicList(hearings, hearings.get(0).getId());

        DynamicList expected = dynamicList(hearings.get(0).getId(),
            hearings.get(1).getId(),
            hearings.get(2).getId(),
            true);

        assertThat(dynamicList).isEqualTo(expected);
    }

    @Test
    void shouldReturnCorrectPageDataWhenThereAreMultipleHearings() {
        List<Element<HearingBooking>> hearings = hearings();

        Map<String, Object> initialPageData = service.getInitialPageData(hearings, List.of());

        Map<String, Object> expected = Map.of(
            "pastHearingList", dynamicList(hearings.get(0).getId(), hearings.get(1).getId(), hearings.get(2).getId()),
            "numHearings", "MULTI"
        );

        assertThat(initialPageData).isEqualTo(expected);
    }

    @Test
    void shouldReturnCorrectPageDataWhenThereAreMultipleHearingsWithSomeAlreadyAssigned() {
        List<Element<HearingBooking>> hearings = new ArrayList<>(hearings());

        Element<CaseManagementOrder> cmo = element(CaseManagementOrder.builder().build());
        Element<HearingBooking> hearing = element(
            hearing(CASE_MANAGEMENT, LocalDateTime.of(2020, 1, 15, 0, 0), cmo.getId())
        );

        hearings.add(hearing);

        Map<String, Object> initialPageData = service.getInitialPageData(hearings, List.of(cmo));

        Map<String, Object> expected = Map.of(
            "pastHearingList", dynamicList(hearings.get(0).getId(), hearings.get(1).getId(), hearings.get(2).getId()),
            "numHearings", "MULTI",
            "multiHearingsWithCMOs", "Case management hearing, 15 January 2020",
            "showHearingsMultiTextArea", "YES"
        );

        assertThat(initialPageData).isEqualTo(expected);
    }

    @Test
    void shouldReturnCorrectPageDataWhenThereIsOneRemainingHearing() {
        List<Element<HearingBooking>> hearings = List.of(element(
            hearing(CASE_MANAGEMENT, LocalDateTime.of(2020, 2, 1, 0, 0))
        ));

        Map<String, Object> initialPageData = service.getInitialPageData(hearings, List.of());

        Map<String, Object> expected = Map.of(
            "numHearings", "SINGLE",
            "cmoHearingInfo", "Send agreed CMO for Case management hearing, 1 February 2020."
                + "\nThis must have been discussed by all parties at the hearing.",
            "cmoJudgeInfo", "His Honour Judge Dredd"
        );

        assertThat(initialPageData).isEqualTo(expected);
    }

    @Test
    void shouldReturnCorrectPageDataWhenThereIsOneRemainingHearingWithSomeAlreadyAssigned() {
        Element<CaseManagementOrder> cmo = element(CaseManagementOrder.builder().build());
        List<Element<HearingBooking>> hearings = List.of(
            element(hearing(CASE_MANAGEMENT, LocalDateTime.of(2020, 2, 1, 0, 0))),
            element(hearing(CASE_MANAGEMENT, LocalDateTime.of(2020, 2, 2, 0, 0), cmo.getId()))
        );

        Map<String, Object> initialPageData = service.getInitialPageData(hearings, List.of(cmo));

        Map<String, Object> expected = Map.of(
            "numHearings", "SINGLE",
            "cmoHearingInfo", "Send agreed CMO for Case management hearing, 1 February 2020."
                + "\nThis must have been discussed by all parties at the hearing.",
            "cmoJudgeInfo", "His Honour Judge Dredd",
            "singleHearingsWithCMOs", "Case management hearing, 2 February 2020",
            "showHearingsSingleTextArea", "YES"
        );

        assertThat(initialPageData).isEqualTo(expected);
    }

    @Test
    void shouldReturnCorrectPageDataWhenThereAreNoRemainingHearings() {
        Map<String, Object> initialPageData = service.getInitialPageData(List.of(), List.of());

        Map<String, String> expected = Map.of(
            "numHearings", "NONE"
        );

        assertThat(initialPageData).isEqualTo(expected);
    }

    @Test
    void shouldReturnCorrectPageDataWhenThereAreNoRemainingHearingsWithoutCMOsAssigned() {
        Element<CaseManagementOrder> cmo = element(CaseManagementOrder.builder().build());
        Element<HearingBooking> hearing = element(hearing(
            CASE_MANAGEMENT,
            LocalDateTime.of(2020, 2, 2, 0, 0),
            cmo.getId())
        );

        Map<String, Object> initialPageData = service.getInitialPageData(List.of(hearing), List.of(cmo));

        Map<String, String> expected = Map.of(
            "numHearings", "NONE"
        );

        assertThat(initialPageData).isEqualTo(expected);
    }

    @Test
    void shouldNotIncludeReturnedHearingsInCMOTextArea() {
        List<Element<HearingBooking>> hearings = new ArrayList<>(hearings());

        Element<CaseManagementOrder> cmo = element(CaseManagementOrder.builder().build());
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

        Map<String, Object> initialPageData = service.getInitialPageData(hearings, List.of(cmo, returnedCMO));

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

        Map<String, Object> expected = Map.of(
            "pastHearingList", dynamicList,
            "numHearings", "MULTI",
            "multiHearingsWithCMOs", "Case management hearing, 15 January 2020",
            "showHearingsMultiTextArea", "YES"
        );

        assertThat(initialPageData).isEqualTo(expected);
    }

    @Test
    void shouldReturnJudgeNameAndTitleAndHearingInfo() {
        List<Element<HearingBooking>> hearings = hearings();
        Map<String, Object> details = service.getJudgeAndHearingDetails(hearings.get(0).getId(), hearings);

        assertThat(details).isEqualTo(Map.of(
           "cmoHearingInfo", "Case management hearing, 2 March 2020",
            "cmoJudgeInfo", "His Honour Judge Dredd"
        ));
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

    private HearingBooking hearing() {
        return hearing(null);
    }

    private HearingBooking hearing(UUID cmoId) {
        return hearing(null, null, cmoId);
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
