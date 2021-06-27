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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;

@ExtendWith(SpringExtension.class)
class OthersServiceTest {
    private static final UUID ID = randomUUID();

    private final OthersService service = new OthersService();

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
                .firstOther(buildOther("First other"))
                .additionalOthers(List.of(element(buildOther("Second other"))))
                .build())
            .sendOrderToAllOthers("Yes")
                .build();

        List<Element<Other>> selectedOthers = service.getSelectedOthers(caseData);

        assertThat(selectedOthers.get(0).getValue()).isEqualTo(caseData.getAllOthers().get(0).getValue());
        assertThat(selectedOthers.get(1).getValue()).isEqualTo(caseData.getAllOthers().get(1).getValue());
    }

    @Test
    void shouldReturnSelectedOthersOnly() {
        int selectedOther = 1;
        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(buildOther("First other"))
                .additionalOthers(List.of(element(buildOther("Second other"))))
                .build())
            .othersSelector(Selector.builder().selected(List.of(selectedOther)).build())
            .sendOrderToAllOthers("No")
            .build();

        List<Element<Other>> selectedOthers = service.getSelectedOthers(caseData);

        assertThat(selectedOthers).containsExactly(caseData.getAllOthers().get(selectedOther));
    }

    @Test
    void shouldReturnTrueWhenRepresented() {
        Other other = buildOther("First other");
        other.addRepresentative(UUID.randomUUID());

        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(other)
                .build())
            .build();

        boolean isRepresented = service.isRepresented(caseData.getAllOthers().get(0).getValue());

        assertTrue(isRepresented);
    }

    @Test
    void shouldReturnFalseWhenNotRepresented() {
        Other other = buildOther("First other");

        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(other)
                .build())
            .build();

        boolean isRepresented = service.isRepresented(caseData.getAllOthers().get(0).getValue());

        assertFalse(isRepresented);
    }

    @Test
    void shouldReturnTrueWhenHasAddressAdded() {
        Other other = buildOther("First other");

        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(other)
                .build())
            .build();

        boolean hasAddressAdded = service.hasAddressAdded(caseData.getAllOthers().get(0).getValue());

        assertTrue(hasAddressAdded);
    }

    @Test
    void shouldReturnFalseWhenAddressIsNotPresent() {
        Other other = Other.builder()
            .name("First other")
            .address(Address.builder().build())
            .build();

        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(other)
                .build())
            .build();

        boolean hasAddressAdded = service.hasAddressAdded(caseData.getAllOthers().get(0).getValue());

        assertFalse(hasAddressAdded);
    }

    private Other buildOther(String name) {
        return Other.builder()
            .name(name)
            .address(testAddress())
            .build();
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
