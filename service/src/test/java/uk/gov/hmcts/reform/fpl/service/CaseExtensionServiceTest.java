package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildExtension;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.event.ChildExtensionEventData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class CaseExtensionServiceTest {
    private static final LocalDate DATE_SUBMITTED = LocalDate.of(2020, 1, 1);
    private static final LocalDate OTHER_DATE = LocalDate.of(2020, 3, 3);

    @Mock
    private ValidateGroupService validateGroupService;

    @InjectMocks
    private CaseExtensionService service;

    @Test
    void shouldGetCaseCompletedByDateWhenNoCompletionDate() {
        CaseData data = CaseData.builder().dateSubmitted(DATE_SUBMITTED).build();

        LocalDate caseCompletionDate = service.getCaseShouldBeCompletedByDate(data);

        assertThat(caseCompletionDate).isEqualTo(DATE_SUBMITTED.plusWeeks(26));
    }

    @Test
    void shouldGetCaseCompletedByDateWhenCompletionDateExists() {

        CaseData data = CaseData.builder()
            .dateSubmitted(DATE_SUBMITTED)
            .caseCompletionDate(OTHER_DATE)
            .build();

        LocalDate caseCompletionDate = service.getCaseShouldBeCompletedByDate(data);

        assertThat(caseCompletionDate).isEqualTo(OTHER_DATE);
    }

    @Test
    void shouldReturnPrePopulatedFields() {
        List<Child> children = List.of(
                getChild(LocalDate.of(2024, 2, 2), "Daisy", "French"),
                getChild(null, "Archie", "Turner"),
                getChild(LocalDate.of(2025, 4, 8), "Julie", "Jane")
        );
        CaseData caseData = CaseData.builder()
                .children1(ElementUtils.wrapElements(children))
                .dateSubmitted(LocalDate.of(2023, 10, 2))
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
                .containsEntry("shouldBeCompletedByDate", "1 April 2024");
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
                getChild(LocalDate.of(2024, 2, 2), "Daisy", "French"),
                getChild(null, "Archie", "Turner"),
                getChild(LocalDate.of(2025, 4, 8), "Julie", "Jane")
        );
        CaseData caseData = CaseData.builder()
                .children1(ElementUtils.wrapElements(children))
                .dateSubmitted(LocalDate.of(2023, 10, 2))
                .childExtensionEventData(childExtensionEventData)
                .build();

        Map<String, Object> selectedChildren = service.getSelectedChildren(caseData);

        assertThat(selectedChildren)
                .containsEntry("childSelected1", YES.getValue())
                .containsEntry("childSelected2", YES.getValue())
                .containsEntry("childExtension1", ChildExtension.builder()
                        .label("Archie Turner")
                        .build())
                .containsEntry("childExtension2", ChildExtension.builder()
                        .label("Julie Jane")
                        .build());
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
                getChild(LocalDate.of(2024, 2, 2), "Daisy", "French"),
                getChild(null, "Archie", "Turner"),
                getChild(LocalDate.of(2025, 4, 8), "Julie", "Jane")
        );
        CaseData caseData = CaseData.builder()
                .children1(ElementUtils.wrapElements(children))
                .dateSubmitted(LocalDate.of(2023, 10, 2))
                .childExtensionEventData(childExtensionEventData)
                .build();

        Map<String, Object> selectedChildren = service.getSelectedChildren(caseData);

        assertThat(selectedChildren)
                .containsEntry("childSelected0", YES.getValue())
                .containsEntry("childSelected1", YES.getValue())
                .containsEntry("childSelected2", YES.getValue())
                .containsEntry("childExtension0", ChildExtension.builder()
                        .label("Daisy French")
                        .build())
                .containsEntry("childExtension1", ChildExtension.builder()
                        .label("Archie Turner")
                        .build())
                .containsEntry("childExtension2", ChildExtension.builder()
                        .label("Julie Jane")
                        .build());
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


    private Child getChild(LocalDate completionDate,
                           String firstName,
                           String lastName) {
        ChildParty childParty = ChildParty.builder()
                .completionDate(completionDate)
                .firstName(firstName)
                .lastName(lastName)
                .build();
        return Child.builder()
                .party(childParty)
                .build();
    }
}
