package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOther;

@ExtendWith(SpringExtension.class)
class OthersServiceTest {
    private static final UUID ID = randomUUID();

    private final OthersService service = new OthersService();

    @Test
    void shouldBuildExpectedOtherSelectorWhenNoOthers() {
        List<Other> allOthers = emptyList();
        List<Other> selectedOthers = emptyList();

        Selector expectedSelector = Selector.builder().build();
        Selector result = service.buildOtherSelector(allOthers, selectedOthers);

        assertThat(result).isEqualTo(expectedSelector);
    }

    @Test
    void shouldBuildExpectedOtherSelectorWhenNoSelectedOthers() {
        List<Other> allOthers = List.of(Other.builder().build());
        List<Other> selectedOthers = emptyList();

        Selector expectedSelector = Selector.builder().build().setNumberOfOptions(1);
        Selector result = service.buildOtherSelector(allOthers, selectedOthers);

        assertThat(result).isEqualTo(expectedSelector);
    }

    @Test
    void shouldBuildExpectedOtherSelectorWhenSingleSelectedOthers() {
        Other other = Other.builder().build();
        List<Other> allOthers = List.of(other);
        List<Other> selectedOthers = List.of(other);

        Selector expectedSelector = Selector.builder().selected(List.of(0)).build().setNumberOfOptions(1);
        Selector result = service.buildOtherSelector(allOthers, selectedOthers);

        assertThat(result).isEqualTo(expectedSelector);
    }

    @Test
    void shouldBuildExpectedOtherSelectorWhenMultipleSelectedOthers() {
        Other firstOther = Other.builder().name("Huey").build();
        Other secondOther = Other.builder().name("Dewey").build();
        Other thirdOther = Other.builder().name("Louie").build();

        List<Other> allOthers = List.of(firstOther, secondOther, thirdOther);
        List<Other> selectedOthers = List.of(firstOther, thirdOther);

        Selector expectedSelector = Selector.builder().selected(List.of(0,2)).build().setNumberOfOptions(3);
        Selector result = service.buildOtherSelector(allOthers, selectedOthers);

        assertThat(result).isEqualTo(expectedSelector);
    }

    @Test
    void shouldBuildExpectedLabelWhenSingleElementInList() {
        Others others = Others.builder()
            .firstOther(Other.builder()
                .name("James Daniels")
                .build())
            .build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - James Daniels\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenSingleElementInListWithEmptyName() {
        Others others = Others.builder().firstOther(Other.builder().birthPlace("birth place").build()).build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - \n");
    }

    @Test
    void shouldBuildExpectedLabelWhenManyElementsInList() {
        Others others = Others.builder()
            .firstOther(Other.builder().name("James Daniels").build())
            .additionalOthers(wrapElements((Other.builder().name("Bob Martyn").build())))
            .build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - James Daniels\nOther person 1 - Bob Martyn\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenManyElementsInListWithEmptyName() {
        Others others = Others.builder()
            .firstOther(Other.builder().birthPlace("birth place").build())
            .additionalOthers(wrapElements(Other.builder().birthPlace("birth place").build()))
            .build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - \nOther person 1 - \n");
    }

    @Test
    void shouldBuildExpectedLabelWhenNull() {
        String result = service.buildOthersLabel(null);

        assertThat(result).isEqualTo("No others on the case");
    }

    @Test
    void shouldBuildExpectedLabelWhenEmptyOthers() {
        String result = service.buildOthersLabel(Others.builder().build());

        assertThat(result).isEqualTo("No others on the case");
    }

    @Test
    void shouldReturnEmptyOthersWhenNoOthersInCaseData() {
        CaseData caseData = CaseData.builder().build();

        Others others = service.prepareOthers(caseData);

        assertThat(others).isEqualTo(Others.builder().additionalOthers(emptyList()).build());
    }

    @Test
    void shouldReturnOthersWhenOthersIsPrePopulated() {
        List<Element<Other>> additionalOthers = othersWithRemovedConfidentialFields();

        CaseData caseData = buildCaseDataWithOthers(otherWithDetailsHiddenValue("No"), additionalOthers, null);

        Others others = service.prepareOthers(caseData);

        assertThat(caseData.getOthers()).isEqualTo(others);
    }

    @Test
    void shouldPrepareOthersWithConfidentialValuesWhenConfidentialOthersIsNotEmpty() {
        List<Element<Other>> additionalOthersList = othersWithConfidentialFields(randomUUID());

        Other firstOther = othersWithRemovedConfidentialFields().get(0).getValue();
        List<Element<Other>> confidentialOthers = othersWithConfidentialFields(ID);

        CaseData caseData = buildCaseDataWithOthers(firstOther, additionalOthersList, confidentialOthers);

        Others others = service.prepareOthers(caseData);

        assertThat(others.getFirstOther()).isEqualTo(confidentialOthers.get(0).getValue());
    }

    @Test
    void shouldReturnOtherWithoutConfidentialDetailsWhenThereIsNoMatchingConfidentialOther() {
        Other firstOther = othersWithRemovedConfidentialFields().get(0).getValue();
        List<Element<Other>> additionalOther = othersWithRemovedConfidentialFields();
        List<Element<Other>> confidentialOther = othersWithConfidentialFields(randomUUID());

        CaseData caseData = buildCaseDataWithOthers(firstOther, additionalOther, confidentialOther);

        Others others = service.prepareOthers(caseData);

        assertThat(others.getAdditionalOthers()).containsOnly(additionalOther.get(0));
    }

    @Test
    void shouldAddExpectedOtherWhenHiddenDetailsMarkedAsNo() {
        Other firstOther = otherWithDetailsHiddenValue("No");
        List<Element<Other>> confidentialOther = othersWithConfidentialFields(ID);

        CaseData caseData = buildCaseDataWithOthers(firstOther, null, confidentialOther);

        Others others = service.prepareOthers(caseData);

        assertThat(others.getFirstOther()).isEqualTo(otherWithDetailsHiddenValue("No"));
        assertThat(others.getAdditionalOthers()).isEmpty();
    }

    @Test
    void shouldMaintainOrderingOfOthersWhenPreparingOthersWithConfidential() {
        UUID otherId = randomUUID();

        List<Element<Other>> others = List.of(
            othersWithRemovedConfidentialFields().get(0),
            othersWithConfidentialFields(otherId).get(0));

        List<Element<Other>> confidentialOthers = List.of(othersWithConfidentialFields(otherId).get(0));

        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(othersWithRemovedConfidentialFields().get(0).getValue())
                .additionalOthers(others)
                .build())
            .confidentialOthers(confidentialOthers)
            .build();

        Others updatedOthers = service.prepareOthers(caseData);

        assertThat(updatedOthers.getAdditionalOthers().get(0).getValue()).isEqualTo(others.get(0).getValue());
        assertThat(updatedOthers.getAdditionalOthers().get(1).getValue()).isEqualTo(others.get(1).getValue());
    }

    @Test
    void shouldReturnAllOthersWhenUseAllOthers() {
        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(testOther("First other"))
                .additionalOthers(List.of(element(testOther("Second other"))))
                .build())
            .sendOrderToAllOthers("Yes")
                .build();

        List<Element<Other>> selectedOthers = service.getSelectedOthers(caseData);

        assertThat(selectedOthers.get(0).getValue()).isEqualTo(caseData.getAllOthers().get(0).getValue());
        assertThat(selectedOthers.get(1).getValue()).isEqualTo(caseData.getAllOthers().get(1).getValue());
    }

    @Test
    void shouldReturnEmptyListWhenSelectorIsNull() {
        CaseData caseData = CaseData.builder()
            .sendOrderToAllOthers("No")
            .othersSelector(null)
            .build();

        List<Element<Other>> selectedOthers = service.getSelectedOthers(caseData);

        assertThat(selectedOthers).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenSelectedIsEmpty() {
        CaseData caseData = CaseData.builder()
            .othersSelector(Selector.builder().selected(emptyList()).build())
            .sendOrderToAllOthers("No")
            .build();

        List<Element<Other>> selectedOthers = service.getSelectedOthers(caseData);

        assertThat(selectedOthers).isEmpty();
    }

    @Test
    void shouldBuildExpectedLabelWhenEmptyList() {
        String label = service.getOthersLabel(List.of());
        assertThat(label).isEqualTo("");
    }

    @Test
    void shouldBuildExpectedLabelWhenPopulatedList() {
        List<Element<Other>> others = List.of(element(testOther("First other")),
            element(testOther("Second other")));

        String label = service.getOthersLabel(others);
        assertThat(label).isEqualTo("Other 1: First other\n"
            + "Other 2: Second other\n");
    }

    @Test
    void shouldReturnSelectedOthersOnly() {
        int selectedOther = 1;
        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(testOther("First other"))
                .additionalOthers(List.of(element(testOther("Second other"))))
                .build())
            .othersSelector(Selector.builder().selected(List.of(selectedOther)).build())
            .sendOrderToAllOthers("No")
            .build();

        List<Element<Other>> selectedOthers = service.getSelectedOthers(caseData);

        assertThat(selectedOthers).containsExactly(caseData.getAllOthers().get(selectedOther));
    }

    private CaseData buildCaseDataWithOthers(Other firstOther,
                                             List<Element<Other>> additionalOthers,
                                             List<Element<Other>> confidentialOthers) {
        return CaseData.builder()
            .others(Others.builder().firstOther(firstOther).additionalOthers(additionalOthers).build())
            .confidentialOthers(confidentialOthers)
            .build();
    }

    private Other otherWithDetailsHiddenValue(String hidden) {
        return Other.builder()
            .name("James")
            .gender("Female")
            .detailsHidden(hidden)
            .address(Address.builder().addressLine1("Address Line 1").build())
            .telephone("01227 831393")
            .build();
    }

    private Other confidentialOther() {
        return Other.builder()
            .name("James")
            .address(Address.builder().addressLine1("Address Line 1").build())
            .telephone("01227 831393")
            .build();
    }

    private List<Element<Other>> othersWithConfidentialFields(UUID id) {
        return newArrayList(element(id, Other.builder()
            .name("James")
            .gender("Female")
            .detailsHidden("Yes")
            .address(Address.builder().addressLine1("Address Line 1").build())
            .telephone("01227 831393")
            .build()));
    }

    private List<Element<Other>> othersWithRemovedConfidentialFields() {
        return newArrayList(element(ID, Other.builder()
            .name("James")
            .gender("Female")
            .detailsHidden("Yes")
            .build()));
    }
}
