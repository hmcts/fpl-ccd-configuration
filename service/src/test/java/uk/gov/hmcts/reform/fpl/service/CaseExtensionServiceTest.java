package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildExtension;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ChildExtensionEventData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.hearing.HearingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList.INTERNATIONAL_ASPECT;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList.NO_EXTENSION;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime.EIGHT_WEEK_EXTENSION;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime.OTHER_EXTENSION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class CaseExtensionServiceTest {
    @Mock
    private ValidateGroupService validateGroupService;

    @Mock
    private HearingService hearingService;

    @InjectMocks
    private CaseExtensionService service;

    private final Element<HearingBooking> hearingBooking = element(UUID.randomUUID(), HearingBooking.builder()
        .startDate(LocalDateTime.of(2020, 1, 1, 12, 0))
        .endDate(LocalDateTime.of(2020, 1, 1, 13, 0))
        .type(HearingType.CASE_MANAGEMENT)
        .build());

    @Test
    void shouldReturnPrePopulatedFields() {
        List<Child> children = List.of(
            getChild(of(2024, 2, 2), "Daisy", "French"),
            getChild(null, "Archie", "Turner"),
            getChild(of(2025, 4, 8), "Julie", "Jane")
        );
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(children))
            .dateSubmitted(of(2023, 10, 2))
            .hearingDetails(List.of(hearingBooking))
            .build();

        Map<String, Object> prePopulateFields = service.prePopulateFields(caseData);

        String expectedLabel = String.join(System.lineSeparator(),
                "Child 1: Daisy French: 2 February 2024",
                "Child 2: Archie Turner: 1 April 2024",
                "Child 3: Julie Jane: 8 April 2025");

        assertThat(prePopulateFields)
            .containsEntry("childCaseCompletionDateLabel", expectedLabel)
            .containsEntry("childSelectorForExtension", Selector.builder()
                .count("123")
                .build())
            .containsEntry("shouldBeCompletedByDate", "8 April 2025")
            .containsEntry("extendTimelineHearingList", DynamicList.builder()
                .value(DynamicListElement.EMPTY)
                .listItems(List.of(DynamicListElement.builder()
                    .code(hearingBooking.getId())
                    .label(hearingBooking.getValue().toLabel())
                    .build()))
                .build());
    }

    @Test
    void shouldReturnSelectedChildren() {
        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
                .extensionForAllChildren(NO.getValue())
                .childSelectorForExtension(Selector.builder()
                        .selected(List.of(1,2))
                        .build())
                .build();

        List<Child> children = List.of(
                getChild(of(2024, 2, 2), "Daisy", "French"),
                getChild(null, "Archie", "Turner"),
                getChild(of(2025, 4, 8), "Julie", "Jane")
        );
        CaseData caseData = CaseData.builder()
                .children1(wrapElements(children))
                .dateSubmitted(of(2023, 10, 2))
                .childExtensionEventData(childExtensionEventData)
                .build();

        Map<String, Object> selectedChildren = service.getSelectedChildren(caseData);

        assertThat(selectedChildren)
            .containsEntry("childSelected1", YES.getValue())
            .containsEntry("childSelected2", YES.getValue())
            .containsEntry("childExtension1", ChildExtension.builder()
                    .label("Archie Turner")
                    .index("2")
                    .build())
            .containsEntry("childExtension2", ChildExtension.builder()
                    .label("Julie Jane")
                    .index("3")
                    .build());
    }

    @Test
    void shouldReturnAllChildren() {
        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
                .extensionForAllChildren(NO.getValue())
                .childSelectorForExtension(Selector.builder()
                        .selected(List.of(1,2))
                        .build())
                .build();

        List<Child> children = List.of(
                getChild(of(2024, 2, 2), "Daisy", "French"),
                getChild(null, "Archie", "Turner"),
                getChild(of(2025, 4, 8), "Julie", "Jane")
        );
        CaseData caseData = CaseData.builder()
                .children1(wrapElements(children))
                .dateSubmitted(of(2023, 10, 2))
                .childExtensionEventData(childExtensionEventData)
                .build();

        Map<String, Object> selectedChildren = service.getAllChildren(caseData);

        assertThat(selectedChildren)
                .containsEntry("childSelected0", YES.getValue())
                .containsEntry("childSelected1", YES.getValue())
                .containsEntry("childSelected2", YES.getValue())
                .containsEntry("childExtension0", ChildExtension.builder()
                    .label("Daisy French")
                    .index("1")
                    .build())
                .containsEntry("childExtension1", ChildExtension.builder()
                    .label("Archie Turner")
                    .index("2")
                    .build())
                .containsEntry("childExtension2", ChildExtension.builder()
                    .label("Julie Jane")
                    .index("3")
                    .build());
    }

    @Test
    void shouldUpdateSelectedChildren() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        when(hearingService.findHearing(any(), any())).thenReturn(Optional.of(hearingBooking));

        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
                .extendTimelineApprovedAtHearing(YES)
                .extendTimelineHearingList(getHearingList(hearingBooking.getId()))
                .extensionForAllChildren(NO.getValue())
                .childSelectorForExtension(Selector.builder()
                    .selected(List.of(0, 1, 2))
                    .build())
                .childExtension0(ChildExtension.builder()
                    .id(id1)
                    .caseExtensionTimeList(EIGHT_WEEK_EXTENSION)
                    .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                    .build())
               .childExtension1(ChildExtension.builder()
                    .id(id2)
                    .caseExtensionTimeList(OTHER_EXTENSION)
                    .extensionDateOther(of(2024, 3, 4))
                    .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                    .build())
               .childExtension3(ChildExtension.builder()
                    .id(id3)
                    .caseExtensionTimeList(EIGHT_WEEK_EXTENSION)
                    .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                    .build())
                .build();

        List<Element<Child>> children1 = List.of(
                element(id1, getChild(of(2024, 2, 2), "Daisy", "French")),
                element(id2, getChild(null, "Archie", "Turner")),
                element(id3, getChild(of(2025, 4, 8), "Julie", "Jane"))
        );

        CaseData caseData = CaseData.builder()
                .children1(children1)
                .hearingDetails(List.of(hearingBooking))
                .dateSubmitted(of(2023, 10, 2))
                .childExtensionEventData(childExtensionEventData)
                .build();


        List<Element<Child>> selectedChildren = service.updateChildrenExtension(caseData);

        assertThat(selectedChildren).contains(
                element(id1, getChild(of(2020, 2, 26), "Daisy", "French")),
                element(id2, getChild(of(2024, 3, 4), "Archie", "Turner")),
                element(id3, getChild(of(2020, 2, 26), "Julie", "Jane"))
        );
    }

    @Test
    void shouldUpdateAllChildrenWithOtherExtension() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();
        LocalDate extensionDateOther = of(2031, 9, 12);
        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
            .extendTimelineApprovedAtHearing(NO)
            .extendTimelineHearingDate(LocalDate.of(2030, 1, 1))
            .extensionForAllChildren(YES.getValue())
                .sameExtensionForAllChildren(YES.getValue())
                .childSelectorForExtension(Selector.builder()
                        .selected(List.of(1,2))
                        .build())
                .childExtensionAll(ChildExtension.builder()
                        .caseExtensionTimeList(OTHER_EXTENSION)
                        .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                        .extensionDateOther(extensionDateOther)
                        .build())
                .build();


        List<Element<Child>> children1 = List.of(
                element(id1, getChild(of(2024, 2, 2), "Daisy", "French")),
                element(id2, getChild(null, "Archie", "Turner")),
                element(id3, getChild(of(2025, 4, 8), "Julie", "Jane"))
        );

        CaseData caseData = CaseData.builder()
                .children1(children1)
                .dateSubmitted(of(2030, 10, 2))
                .childExtensionEventData(childExtensionEventData)
                .build();


        List<Element<Child>> selectedChildren = service.updateAllChildrenExtension(caseData);

        assertThat(selectedChildren).contains(
            element(id1, getChild(of(2031, 9, 12), "Daisy", "French")),
            element(id2, getChild(of(2031, 9, 12), "Archie", "Turner")),
            element(id3, getChild(of(2031, 9, 12), "Julie", "Jane"))
        );
    }

    @Test
    void shouldUpdateAllSelectedChildrenExtensions() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();
        LocalDate extensionDateOther = of(2031, 9, 12);

        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
            .extendTimelineApprovedAtHearing(NO)
            .extendTimelineHearingDate(LocalDate.of(2030, 1, 1))
            .extensionForAllChildren(NO.getValue())
            .sameExtensionForAllChildren(YES.getValue())
            .childSelectorForExtension(Selector.builder()
                    .selected(List.of(1,2))
                    .build())
            .childExtensionAll(ChildExtension.builder()
                    .caseExtensionTimeList(OTHER_EXTENSION)
                    .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                    .extensionDateOther(extensionDateOther)
                    .build())
            .build();

        List<Element<Child>> children1 = List.of(
            element(id1, getChild(of(2024, 2, 2), "Daisy", "French")),
            element(id2, getChild(null, "Archie", "Turner")),
            element(id3, getChild(of(2025, 4, 8), "Julie", "Jane"))
        );

        CaseData caseData = CaseData.builder()
            .children1(children1)
            .dateSubmitted(of(2030, 10, 2))
            .childExtensionEventData(childExtensionEventData)
            .build();


        List<Element<Child>> selectedChildren = service.updateAllSelectedChildrenExtension(caseData);


        assertThat(selectedChildren).contains(
            element(id1, getChild(of(2024, 2, 2), "Daisy", "French")),
            element(id2, getChild(of(2031, 9, 12), "Archie", "Turner")),
            element(id3, getChild(of(2031, 9, 12), "Julie", "Jane"))
        );
    }

    @Test
    void shouldUpdateAllChildrenWithEigthWeeksExtension() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        when(hearingService.findHearing(any(), any())).thenReturn(Optional.of(hearingBooking));

        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
                .extendTimelineApprovedAtHearing(YES)
                .extendTimelineHearingList(getHearingList(hearingBooking.getId()))
                .extensionForAllChildren(NO.getValue())
                .sameExtensionForAllChildren(YES.getValue())
                .childSelectorForExtension(Selector.builder()
                        .selected(List.of(0,1,2))
                        .build())
                .childExtensionAll(ChildExtension.builder()
                        .caseExtensionTimeList(EIGHT_WEEK_EXTENSION)
                        .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                        .build())
                .build();


        List<Element<Child>> children1 = List.of(
                element(id1, getChild(of(2024, 2, 2), "Daisy", "French")),
                element(id2, getChild(null, "Archie", "Turner")),
                element(id3, getChild(of(2025, 4, 8), "Julie", "Jane"))
        );

        CaseData caseData = CaseData.builder()
                .children1(children1)
                .dateSubmitted(of(2030, 10, 2))
                .hearingDetails(List.of(hearingBooking))
                .childExtensionEventData(childExtensionEventData)
                .build();


        List<Element<Child>> selectedChildren = service.updateAllChildrenExtension(caseData);

        assertThat(selectedChildren).contains(
                element(id1, getChild(of(2020, 2, 26), "Daisy", "French")),
                element(id2, getChild(of(2020, 2, 26), "Archie", "Turner")),
                element(id3, getChild(of(2020, 2, 26), "Julie", "Jane"))
        );
    }

    @Test
    void shouldReturnSelectedAllChildren() {
        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
            .extensionForAllChildren(NO.getValue())
            .childSelectorForExtension(Selector.builder()
                    .selected(List.of(0,1,2))
                    .build())
            .build();

        List<Child> children = List.of(
            getChild(of(2024, 2, 2), "Daisy", "French"),
            getChild(null, "Archie", "Turner"),
            getChild(of(2025, 4, 8), "Julie", "Jane")
        );
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(children))
            .dateSubmitted(of(2023, 10, 2))
            .childExtensionEventData(childExtensionEventData)
            .build();

        Map<String, Object> selectedChildren = service.getSelectedChildren(caseData);

        assertThat(selectedChildren)
            .containsEntry("childSelected0", YES.getValue())
            .containsEntry("childSelected1", YES.getValue())
            .containsEntry("childSelected2", YES.getValue())
            .containsEntry("childExtension0", ChildExtension.builder()
                .label("Daisy French")
                .index("1")
                .build())
            .containsEntry("childExtension1", ChildExtension.builder()
                .label("Archie Turner")
                .index("2")
                .build())
            .containsEntry("childExtension2", ChildExtension.builder()
                .label("Julie Jane")
                .index("3")
                .build());
    }

    @Test
    void shouldReturnCaseSummaryExtensionDetails() {
        List<Child> children = List.of(
            getChild(of(2024, 2, 2), "Daisy", "French"),
            getChild(null, "Archie", "Turner", NO_EXTENSION),
            getChild(of(2025, 4, 8), "Julie", "Jane")
        );
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(children))
            .dateSubmitted(of(2023, 10, 2))
            .build();

        String caseSummaryExtensionDetails = service.getCaseSummaryExtensionDetails(caseData, wrapElements(children));
        String expectedLabel = String.join(System.lineSeparator(),
                "Daisy French - 2 February 2024 - International Aspect",
                "Archie Turner - 1 April 2024 - No extension for child",
                "Julie Jane - 8 April 2025 - International Aspect");

        assertThat(caseSummaryExtensionDetails).isEqualTo(expectedLabel);
    }

    @Test
    void shouldReturnMaxExtendedTimeLine() {
        List<Child> children = List.of(
            getChild(of(2024, 2, 2), "Daisy", "French"),
            getChild(null, "Archie", "Turner"),
            getChild(of(2025, 4, 8), "Julie", "Jane")
        );
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(children))
            .dateSubmitted(of(2023, 10, 2))
            .build();

        LocalDate maxExtendedTimeLine = service.getMaxExtendedTimeLine(caseData, wrapElements(children));
        assertThat(maxExtendedTimeLine).isEqualTo(of(2025, 4, 8));
    }

    @Test
    void shouldReturnVaidateChildExtension() {
        UUID id2 = UUID.randomUUID();
        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
                .extensionForAllChildren(NO.getValue())
                .childSelectorForExtension(Selector.builder()
                        .selected(List.of(0, 1, 2))
                        .build())
                .childExtension3(ChildExtension.builder()
                        .id(id2)
                        .caseExtensionTimeList(CaseExtensionTime.OTHER_EXTENSION)
                        .extensionDateOther(of(2000, 3, 4))
                        .index("4")
                        .build())
                .build();

        List<Child> children = List.of(
            getChild(of(2024, 2, 2), "Daisy", "French"),
            getChild(null, "Archie", "Turner"),
            getChild(of(2025, 4, 8), "Julie", "Jane")
        );
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(children))
            .dateSubmitted(of(2023, 10, 2))
            .childExtensionEventData(childExtensionEventData)
            .build();

        when(validateGroupService.validateGroup(any(), any())).thenReturn(List.of("Enter an end date in the future"));
        List<String> errors = service.validateChildExtensionDate(caseData);
        assertThat(errors).contains("Enter an end date in the future for child 4");
    }

    @Test
    void shouldHaveValidationErrorWhenNoChildSelected() {
        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
                .extensionForAllChildren(NO.getValue())
                .childSelectorForExtension(Selector.builder().build())
                .build();

        CaseData caseData = CaseData.builder()
                .childExtensionEventData(childExtensionEventData)
                .build();

        List<String> errors = service.validateChildSelector(caseData);
        assertThat(errors)
                .contains("Select the children requiring an extension");
    }

    @Test
    void shouldHaveNoValidationErrorWhenChildIsSelected() {
        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
                .extensionForAllChildren(NO.getValue())
                .childSelectorForExtension(Selector.builder()
                        .selected(List.of(1))
                        .build())
                .build();

        CaseData caseData = CaseData.builder()
                .childExtensionEventData(childExtensionEventData)
                .build();

        List<String> errors = service.validateChildSelector(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldHaveNoValidationErrorWhenAllChildrenOptionIsSelected() {
        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
                .extensionForAllChildren(YES.getValue())
                .build();

        CaseData caseData = CaseData.builder()
                .childExtensionEventData(childExtensionEventData)
                .build();

        List<String> errors = service.validateChildSelector(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldGetExtensionDateFromHearing() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(hearingBooking))
            .childExtensionEventData(ChildExtensionEventData.builder()
                .extendTimelineApprovedAtHearing(YES)
                .extendTimelineHearingList(getHearingList(hearingBooking.getId()))
                .build())
            .build();

        when(hearingService.findHearing(any(), any())).thenReturn(Optional.of(hearingBooking));

        Optional<LocalDate> extensionDate = service.getExtensionDate(caseData);

        assertThat(extensionDate).contains(of(2020, 1, 1));
    }

    @Test
    void shouldGetExtensionDateFromApprovalDate() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(hearingBooking))
            .childExtensionEventData(ChildExtensionEventData.builder()
                .extendTimelineApprovedAtHearing(NO)
                .extendTimelineHearingDate(LocalDate.of(2022, 2, 2))
                .build())
            .build();

        Optional<LocalDate> extensionDate = service.getExtensionDate(caseData);
        assertThat(extensionDate).contains(of(2022, 2, 2));
    }

    private DynamicList getHearingList(UUID selected) {
        return DynamicList.builder()
            .listItems(List.of(DynamicListElement.builder()
                .code(hearingBooking.getId())
                .label(hearingBooking.getValue().toLabel())
                .build()))
            .value(DynamicListElement.builder().code(selected).build())
            .build();
    }

    private Child getChild(LocalDate completionDate,
                           String firstName,
                           String lastName) {
        return getChild(completionDate, firstName, lastName, INTERNATIONAL_ASPECT);
    }

    private Child getChild(LocalDate completionDate,
                           String firstName,
                           String lastName,
                           CaseExtensionReasonList extensionReason) {
        ChildParty childParty = ChildParty.builder()
            .completionDate(completionDate)
            .extensionReason(extensionReason)
            .firstName(firstName)
            .lastName(lastName)
            .build();
        return Child.builder()
            .party(childParty)
            .build();
    }
}
