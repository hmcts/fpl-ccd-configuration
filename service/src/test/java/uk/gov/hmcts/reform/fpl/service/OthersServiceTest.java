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
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
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
    void shouldNotRemoveRepresentedByWhenPrepareConfidentialOthers() {
        List<Element<Other>> confidentialOthers = new ArrayList<>();
        Other firstOther = othersWithRemovedConfidentialFields().get(0).getValue();
        firstOther.addRepresentative(randomUUID());
        confidentialOthers.addAll(othersWithConfidentialFields(randomUUID()));

        List<Element<Other>> othersWithRemovedConfidentialFields = othersWithRemovedConfidentialFields();
        othersWithRemovedConfidentialFields.forEach(ao -> ao.getValue().addRepresentative(randomUUID()));

        othersWithRemovedConfidentialFields.forEach(ao -> confidentialOthers
            .addAll(othersWithConfidentialFields(ao.getId())));

        CaseData caseData = buildCaseDataWithOthers(firstOther, othersWithRemovedConfidentialFields,
            confidentialOthers);

        Others others = service.prepareOthers(caseData);

        assertThat(others).isNotNull();
        assertThat(others.getFirstOther()).isNotNull();
        assertThat(others.getFirstOther().getRepresentedBy()).hasSize(1);
        assertThat(others.getAdditionalOthers()).isNotNull();
        assertThat(others.getAdditionalOthers()).hasSize(1);
        assertThat(others.getAdditionalOthers().get(0).getValue().getRepresentedBy()).hasSize(1);
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

    private DynamicList buildSingleSelector(int selectedIdx) {
        return buildSingleSelector(selectedIdx, null);
    }

    private DynamicList buildSingleSelector(int selectedIdx, Others others) {

        List<Element<Other>> allElements = new ArrayList<>();
        DynamicList.DynamicListBuilder builder = DynamicList.builder();

        if (!nonNull(others)) {
            others = Others.builder()
                .firstOther(Other.builder().name("First Other").build())
                .additionalOthers(List.of(element(Other.builder().name("Additional Other 1").build())))
                .build();
        }

        if (!nonNull(others.getFirstOther())) {
            throw new IllegalStateException("firstOther must not be null");
        }
        allElements.add(element(others.getFirstOther()));
        if (nonNull(others.getAdditionalOthers())) {
            allElements.addAll(others.getAdditionalOthers());
        }

        List<DynamicListElement> listItems = allElements.stream().map(
            (e) -> DynamicListElement.builder()
                .code(e.getId()).label(e.getValue().getName())
                .build())
            .collect(Collectors.toList());
        builder.listItems(listItems);
        builder.value(listItems.get(selectedIdx));
        return builder.build();
    }

    @Test
    void shouldReturnNullWhenThereIsNoOtherPerson() {
        CaseData caseData = CaseData.builder().build();
        Element<Other> selected = service.getSelectedPreparedOther(caseData, buildSingleSelector(0));
        assertThat(selected).isNull();

        selected = service.getSelectedOther(caseData, buildSingleSelector(0));
        assertThat(selected).isNull();
    }

    @Test
    void shouldReturnSelectedOtherPersonWithoutAdditionalOthers() {
        Others others = Others.builder()
            .firstOther(Other.builder().name("First Other").build())
            .build();

        CaseData caseData = CaseData.builder()
            .others(others)
            .build();
        Element<Other> selected = service.getSelectedPreparedOther(caseData, buildSingleSelector(0, others));
        assertThat(selected).isNotNull();
        assertThat(selected.getValue().getName()).isEqualTo("First Other");

        selected = service.getSelectedOther(caseData, buildSingleSelector(0, others));
        assertThat(selected).isNotNull();
        assertThat(selected.getValue().getName()).isEqualTo("First Other");
    }

    @Test
    void shouldReturnSelectedFirstOtherWithProvidedFirstUUID() {
        Others others = Others.builder()
            .firstOther(Other.builder().name("First Other").build())
            .build();

        CaseData caseData = CaseData.builder()
            .others(others)
            .build();
        UUID firstOtherUUID = randomUUID();
        Element<Other> selected = service.getSelectedOther(caseData, buildSingleSelector(0, others),
            firstOtherUUID);
        assertThat(selected).isNotNull();
        assertThat(selected.getValue().getName()).isEqualTo("First Other");
        assertThat(selected.getId()).isEqualTo(firstOtherUUID);
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
        Others others = Others.builder()
            .firstOther(Other.builder().name("First Other").build())
            .additionalOthers(List.of(
                element(Other.builder().name("Other 2").build()),
                element(Other.builder().name("Other 3").build())
            ))
            .build();

        CaseData caseData = CaseData.builder()
            .others(others)
            .build();
        Element<Other> selected = service.getSelectedPreparedOther(caseData, buildSingleSelector(selectedIdx, others));
        assertThat(selected).isNotNull();
        assertThat(selected.getValue().getName()).isEqualTo(expectedName);
    }

    @Test
    void shouldReturnUnchanged() {
        Other firstOther = Other.builder()
            .name("First Other")
            .address(Address.builder().addressLine1("Address Line 1").build())
            .addressNotKnowReason("Some reason")
            .addressKnowV2(null)
            .build();

        CaseData caseData = buildCaseDataWithOthers(firstOther, null, null);
        Others updatedOthers = service.consolidateAndRemoveHiddenFields(caseData);
        assertThat(updatedOthers).isNotNull();
        assertThat(updatedOthers.getFirstOther()).isEqualTo(firstOther);
    }

    @Test
    void shouldRemoveAddress() {
        Other firstOther = Other.builder()
            .name("First Other")
            .address(Address.builder().addressLine1("Address Line 1").build())
            .addressNotKnowReason("Some reason")
            .addressKnowV2(IsAddressKnowType.NO)
            .build();

        CaseData caseData = buildCaseDataWithOthers(firstOther, null, null);
        Others updatedOthers = service.consolidateAndRemoveHiddenFields(caseData);
        assertThat(updatedOthers).isNotNull();
        assertThat(updatedOthers.getFirstOther().getAddressNotKnowReason()).isNotNull();
        assertThat(updatedOthers.getFirstOther().getAddress()).isNull();
    }

    @Test
    void shouldRemoveAddressNotKnowReason() {
        Other firstOther = Other.builder()
            .name("First Other")
            .address(Address.builder().addressLine1("Address Line 1").build())
            .addressNotKnowReason("Some reason")
            .addressKnowV2(IsAddressKnowType.YES)
            .build();

        CaseData caseData = buildCaseDataWithOthers(firstOther, null, null);
        Others updatedOthers = service.consolidateAndRemoveHiddenFields(caseData);
        assertThat(updatedOthers).isNotNull();
        assertThat(updatedOthers.getFirstOther().getAddressNotKnowReason()).isNull();
        assertThat(updatedOthers.getFirstOther().getAddress()).isNotNull();
    }

    @Test
    void shouldSetConfidentialWhenLiveInRefugeIsSelected() {
        Other firstOther = Other.builder()
            .name("First Other")
            .addressKnowV2(IsAddressKnowType.LIVE_IN_REFUGE)
            .build();

        CaseData caseData = buildCaseDataWithOthers(firstOther, null, null);
        Others updatedOthers = service.consolidateAndRemoveHiddenFields(caseData);
        assertThat(updatedOthers).isNotNull();
        assertThat(updatedOthers.getFirstOther().getDetailsHidden()).isEqualTo(YesNo.YES.getValue());
    }
}
