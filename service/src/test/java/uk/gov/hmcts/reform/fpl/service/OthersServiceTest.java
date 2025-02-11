package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;
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
        Other firstOther = Other.builder().firstName("Huey").build();
        Other secondOther = Other.builder().firstName("Dewey").build();
        Other thirdOther = Other.builder().firstName("Louie").build();

        List<Other> allOthers = List.of(firstOther, secondOther, thirdOther);
        List<Other> selectedOthers = List.of(firstOther, thirdOther);

        Selector expectedSelector = Selector.builder().selected(List.of(0,2)).build().setNumberOfOptions(3);
        Selector result = service.buildOtherSelector(allOthers, selectedOthers);

        assertThat(result).isEqualTo(expectedSelector);
    }

    @Test
    void shouldBuildExpectedLabelWhenSingleElementInList() {
        List<Element<Other>> others = wrapElements(Other.builder()
                .firstName("James")
                .lastName("Daniels")
                .build());

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - James Daniels\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenSingleElementInListWithEmptyName() {
        List<Element<Other>> others = wrapElements(Other.builder().telephone("123456").build());

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - \n");
    }

    @Test
    void shouldBuildExpectedLabelWhenManyElementsInList() {
        List<Element<Other>> others = wrapElements(
            Other.builder().firstName("James Daniels").build(),
            Other.builder().name("Bob Martyn").build(),
            Other.builder().lastName("Only Last").build(),
            Other.builder().firstName("First").lastName("Last").build());

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - James Daniels\nOther person 1 - Bob Martyn\n"
                                     + "Other person 2 - Only Last\nOther person 3 - First Last\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenManyElementsInListWithEmptyName() {
        List<Element<Other>> others = wrapElements(
            Other.builder().telephone("123456").build(),
            Other.builder().telephone("123456").build());

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
        String result = service.buildOthersLabel(List.of());

        assertThat(result).isEqualTo("No others on the case");
    }

    @Test
    void shouldReturnEmptyOthersWhenNoOthersInCaseData() {
        CaseData caseData = CaseData.builder().build();

        List<Element<Other>> others = service.prepareOthers(caseData);

        assertThat(others).isEqualTo(emptyList());
    }

    @Test
    void shouldNotRemoveRepresentedByWhenPrepareConfidentialOthers() {
        List<Element<Other>> others = othersWithRemovedConfidentialFields();
        others.get(0).getValue().addRepresentative(randomUUID());

        List<Element<Other>> confidentialOthers = othersWithConfidentialFields(randomUUID());

        List<Element<Other>> othersWithRemovedConfidentialFields = othersWithRemovedConfidentialFields();
        othersWithRemovedConfidentialFields.forEach(ao -> ao.getValue().addRepresentative(randomUUID()));

        othersWithRemovedConfidentialFields.forEach(ao -> confidentialOthers
            .addAll(othersWithConfidentialFields(ao.getId())));
        others.addAll(othersWithRemovedConfidentialFields);

        CaseData caseData = buildCaseDataWithOthers(others, confidentialOthers);

        List<Element<Other>> preparedOthers = service.prepareOthers(caseData);

        assertThat(preparedOthers).isNotNull();
        assertThat(preparedOthers).hasSize(2);
        assertThat(preparedOthers.get(0)).isNotNull();
        assertThat(preparedOthers.get(0).getValue().getRepresentedBy()).hasSize(1);
        assertThat(preparedOthers.get(1).getValue().getRepresentedBy()).hasSize(1);
    }

    @Test
    void shouldPrepareOthersWithConfidentialValuesWhenConfidentialOthersIsNotEmpty() {
        List<Element<Other>> additionalOthersList = othersWithConfidentialFields(randomUUID(), "John");

        // firstOther should be the "same" (in name) as the first confidential other, if they have conf data
        List<Element<Other>> others = othersWithRemovedConfidentialFields("Jack");
        others.addAll(additionalOthersList);
        List<Element<Other>> confidentialOthers = othersWithConfidentialFields(ID, "Jack");

        CaseData caseData = buildCaseDataWithOthers(others, confidentialOthers);

        List<Element<Other>> preparedOthers = service.prepareOthers(caseData);

        assertThat(preparedOthers.get(0).getValue()).isEqualTo(confidentialOthers.get(0).getValue());
    }

    @Test
    void shouldReturnOtherWithoutConfidentialDetailsWhenThereIsNoMatchingConfidentialOther() {
        List<Element<Other>> others = othersWithRemovedConfidentialFields("James");
        List<Element<Other>> additionalOther = othersWithRemovedConfidentialFields("Jack");
        others.addAll(additionalOther);
        List<Element<Other>> confidentialOther = othersWithConfidentialFields(randomUUID());

        CaseData caseData = buildCaseDataWithOthers(others, confidentialOther);

        List<Element<Other>> preparedOthers = service.prepareOthers(caseData);

        assertThat(preparedOthers.get(1)).isEqualTo(additionalOther.get(0));
    }

    @Test
    void shouldAddExpectedOtherWhenHiddenDetailsMarkedAsNo() {
        Other firstOther = otherWithDetailsHiddenValue("No");
        List<Element<Other>> confidentialOther = othersWithConfidentialFields(ID);

        CaseData caseData = buildCaseDataWithOthers(wrapElements(firstOther), confidentialOther);

        List<Element<Other>> others = service.prepareOthers(caseData);

        assertThat(others.get(0).getValue()).isEqualTo(otherWithDetailsHiddenValue("No"));
        assertThat(others).hasSize(1);
    }

    @Test
    void shouldMaintainOrderingOfOthersWhenPreparingOthersWithConfidential() {
        UUID otherId = randomUUID();

        List<Element<Other>> others = othersWithRemovedConfidentialFields("Jack");
        others.addAll(othersWithRemovedConfidentialFields("James"));
        others.addAll(othersWithConfidentialFields(otherId));

        List<Element<Other>> confidentialOthers = List.of(othersWithConfidentialFields(otherId).get(0));

        CaseData caseData = CaseData.builder()
            .othersV2(others)
            .confidentialOthers(confidentialOthers)
            .build();

        List<Element<Other>> updatedOthers = service.prepareOthers(caseData);

        assertThat(updatedOthers.get(0).getValue()).isEqualTo(others.get(0).getValue());
        assertThat(updatedOthers.get(1).getValue()).isEqualTo(others.get(1).getValue());
        assertThat(updatedOthers.get(2).getValue()).isEqualTo(others.get(2).getValue());
    }

    @Test
    void shouldReturnAllOthersWhenUseAllOthers() {
        CaseData caseData = CaseData.builder()
            .othersV2(wrapElements(testOther("First other"), testOther("Second other")))
            .sendOrderToAllOthers("Yes")
            .build();

        List<Element<Other>> selectedOthers = service.getSelectedOthers(caseData);

        assertThat(selectedOthers.get(0).getValue()).isEqualTo(caseData.getOthersV2().get(0).getValue());
        assertThat(selectedOthers.get(1).getValue()).isEqualTo(caseData.getOthersV2().get(1).getValue());
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
        assertThat(label).isEqualTo("Other 1: First other\nOther 2: Second other\n");
    }

    @Test
    void shouldReturnSelectedOthersOnly() {
        int selectedOther = 1;
        CaseData caseData = CaseData.builder()
            .othersV2(wrapElements(testOther("First other"), testOther("Second other")))
            .othersSelector(Selector.builder().selected(List.of(selectedOther)).build())
            .sendOrderToAllOthers("No")
            .build();

        List<Element<Other>> selectedOthers = service.getSelectedOthers(caseData);

        assertThat(selectedOthers).containsExactly(caseData.getOthersV2().get(selectedOther));
    }

    private CaseData buildCaseDataWithOthers(List<Element<Other>> others,
                                             List<Element<Other>> confidentialOthers) {
        return CaseData.builder()
            .othersV2(others)
            .confidentialOthers(confidentialOthers)
            .build();
    }

    private Other otherWithDetailsHiddenValue(String hidden) {
        return Other.builder()
            .firstName("James")
            .hideAddress(hidden)
            .hideTelephone(hidden)
            .address(Address.builder().addressLine1("Address Line 1").build())
            .telephone("01227 831393")
            .build();
    }

    private List<Element<Other>> othersWithConfidentialFields(UUID id) {
        return othersWithConfidentialFields(id, "Joan");
    }

    private List<Element<Other>> othersWithConfidentialFields(UUID id, String name) {
        return newArrayList(element(id, Other.builder()
            .firstName(name)
            .hideAddress(YesNo.YES.getValue())
            .hideTelephone(YesNo.YES.getValue())
            .address(Address.builder().addressLine1("Address Line 1").build())
            .telephone("01227 831393")
            .build()));
    }

    private List<Element<Other>> othersWithRemovedConfidentialFields() {
        return othersWithRemovedConfidentialFields("James");
    }

    private List<Element<Other>> othersWithRemovedConfidentialFields(String name) {
        return newArrayList(element(ID, Other.builder()
            .firstName(name)
            .hideAddress(YesNo.YES.getValue())
            .hideTelephone(YesNo.YES.getValue())
            .build()));
    }


    private DynamicList buildSingleSelector(int selectedIdx) {
        return buildSingleSelector(selectedIdx, null);
    }

    private DynamicList buildSingleSelector(int selectedIdx, List<Element<Other>> others) {
        DynamicList.DynamicListBuilder builder = DynamicList.builder();

        if (isEmpty(others)) {
            others = wrapElementsWithUUIDs(
                Other.builder().firstName("First Other").build(),
                Other.builder().firstName("Additional Other 1").build());
        }

        List<DynamicListElement> listItems = others.stream().map(
            (e) -> DynamicListElement.builder()
                .code(e.getId()).label(e.getValue().getFullName())
                .build())
            .collect(Collectors.toList());
        builder.listItems(listItems);
        builder.value(listItems.get(selectedIdx));
        return builder.build();
    }

    @Test
    void shouldReturnNullWhenThereIsNoOtherPerson() {
        CaseData caseData = CaseData.builder().build();
        assertThrows(NoSuchElementException.class,
            () -> service.getSelectedPreparedOther(caseData, buildSingleSelector(0)));
        assertThrows(NoSuchElementException.class,
            () -> service.getSelectedOther(caseData, buildSingleSelector(0)));
    }

    @Test
    void shouldReturnSelectedOtherPersonWithoutAdditionalOthers() {
        List<Element<Other>> others = wrapElements(Other.builder().firstName("First Other").build());

        CaseData caseData = CaseData.builder()
            .othersV2(others)
            .build();
        Element<Other> selected = service.getSelectedPreparedOther(caseData, buildSingleSelector(0, others));
        assertThat(selected).isNotNull();
        assertThat(selected.getValue().getFullName()).isEqualTo("First Other");

        selected = service.getSelectedOther(caseData, buildSingleSelector(0, others));
        assertThat(selected).isNotNull();
        assertThat(selected.getValue().getFullName()).isEqualTo("First Other");
    }

    @Test
    void shouldReturnSelectedFirstOtherWithProvidedFirstUUID() {
        List<Element<Other>> others = wrapElementsWithUUIDs(Other.builder().firstName("First Other").build());

        CaseData caseData = CaseData.builder()
            .othersV2(others)
            .build();
        Element<Other> selected = service.getSelectedOther(caseData, buildSingleSelector(0, others));
        assertThat(selected).isNotNull();
        assertThat(selected.getValue().getFullName()).isEqualTo("First Other");
        assertThat(selected.getId()).isEqualTo(others.get(0).getId());
    }

    private static Stream<Arguments> selectedPreparedOthersSource() {
        return Stream.of(
            Arguments.of(0, "First Other"),
            Arguments.of(1, "Other 2"),
            Arguments.of(2, "Other 3")
        );
    }

    @ParameterizedTest
    @MethodSource("selectedPreparedOthersSource")
    void shouldReturnSelectedOtherPerson(int selectedIdx, String expectedName) {
        List<Element<Other>> others = wrapElementsWithUUIDs(
            Other.builder().firstName("First Other").build(),
            Other.builder().firstName("Other 2").build(),
            Other.builder().firstName("Other 3").build());

        CaseData caseData = CaseData.builder()
            .othersV2(others)
            .build();
        Element<Other> selected = service.getSelectedPreparedOther(caseData, buildSingleSelector(selectedIdx, others));
        assertThat(selected).isNotNull();
        assertThat(selected.getValue().getFullName()).isEqualTo(expectedName);
    }

    @Test
    void shouldReturnUnchanged() {
        Other firstOther = Other.builder()
            .firstName("First Other")
            .address(Address.builder().addressLine1("Address Line 1").build())
            .addressNotKnowReason("Some reason")
            .addressKnowV2(null)
            .build();

        CaseData caseData = buildCaseDataWithOthers(wrapElements(firstOther), null);
        List<Element<Other>> updatedOthers = service.consolidateAndRemoveHiddenFields(caseData);
        assertThat(updatedOthers).isNotNull();
        assertThat(updatedOthers.get(0).getValue()).isEqualTo(firstOther);
    }

    @Test
    void shouldRemoveAddress() {
        Other firstOther = Other.builder()
            .firstName("First Other")
            .address(Address.builder().addressLine1("Address Line 1").build())
            .addressNotKnowReason("Some reason")
            .addressKnowV2(IsAddressKnowType.NO)
            .build();

        CaseData caseData = buildCaseDataWithOthers(wrapElements(firstOther), null);
        List<Element<Other>> updatedOthers = service.consolidateAndRemoveHiddenFields(caseData);
        assertThat(updatedOthers).isNotNull();
        assertThat(updatedOthers.get(0).getValue().getAddressNotKnowReason()).isNotNull();
        assertThat(updatedOthers.get(0).getValue().getAddress()).isNull();
    }

    @Test
    void shouldRemoveAddressNotKnowReason() {
        Other firstOther = Other.builder()
            .firstName("First Other")
            .address(Address.builder().addressLine1("Address Line 1").build())
            .addressNotKnowReason("Some reason")
            .addressKnowV2(IsAddressKnowType.YES)
            .build();

        CaseData caseData = buildCaseDataWithOthers(wrapElements(firstOther), null);
        List<Element<Other>> updatedOthers = service.consolidateAndRemoveHiddenFields(caseData);
        assertThat(updatedOthers).isNotNull();
        assertThat(updatedOthers.get(0).getValue().getAddressNotKnowReason()).isNull();
        assertThat(updatedOthers.get(0).getValue().getAddress()).isNotNull();
    }

    @Test
    void shouldSetConfidentialWhenLiveInRefugeIsSelected() {
        Other firstOther = Other.builder()
            .firstName("First Other")
            .addressKnowV2(IsAddressKnowType.LIVE_IN_REFUGE)
            .build();

        CaseData caseData = buildCaseDataWithOthers(wrapElements(firstOther), null);
        List<Element<Other>> updatedOthers = service.consolidateAndRemoveHiddenFields(caseData);
        assertThat(updatedOthers).isNotNull();
        assertThat(updatedOthers.get(0).getValue().getHideAddress()).isEqualTo(YesNo.YES.getValue());
    }
}
