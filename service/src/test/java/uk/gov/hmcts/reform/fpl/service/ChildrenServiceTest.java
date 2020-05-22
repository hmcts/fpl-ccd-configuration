package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

@ExtendWith(SpringExtension.class)
class ChildrenServiceTest {

    private final ChildrenService service = new ChildrenService();

    @Test
    void shouldBuildExpectedLabelWhenEmptyList() {
        String label = service.getChildrenLabel(List.of());
        assertThat(label).isEqualTo("No children in the case");
    }

    @Test
    void shouldBuildExpectedLabelWhenPopulatedList() {
        String label = service.getChildrenLabel(List.of(childWithConfidentialFields(randomUUID())));
        assertThat(label).isEqualTo("Child 1: James\n");
    }

    @Test
    void shouldPopulateCaseDataMapWithYesWhenThereAre2OrMoreChildren() {
        List<Element<Child>> children = new ArrayList<>();
        children.add(childWithConfidentialFields(randomUUID()));
        children.add(childWithConfidentialFields(randomUUID()));

        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
        service.addPageShowToCaseDetails(caseDetails, children);

        assertThat(caseDetails.getData()).extracting("pageShow").isEqualTo("Yes");
    }

    @Test
    void shouldPopulateCaseDataMapWithNoWhenThereIsOneChild() {
        List<Element<Child>> children = new ArrayList<>();
        children.add(childWithConfidentialFields(randomUUID()));

        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
        service.addPageShowToCaseDetails(caseDetails, children);

        assertThat(caseDetails.getData()).extracting("pageShow").isEqualTo("No");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnFalseWhenListEmptyOrNull(List<Element<Child>> list) {
        boolean result = service.allChildrenHaveFinalOrder(list);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAtLeastOneChildDoesNotHaveFinalOrder() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued("Yes"), childWithFinalOrderIssued("No"));

        boolean result = service.allChildrenHaveFinalOrder(children);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAllChildrenHaveFinalOrder() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued("Yes"), childWithFinalOrderIssued("Yes"));

        boolean result = service.allChildrenHaveFinalOrder(children);

        assertThat(result).isTrue();
    }

    @Test
    void shouldPopulateCaseDataMapWithNoWhenThereIsEmptyList() {
        List<Element<Child>> children = new ArrayList<>();

        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
        service.addPageShowToCaseDetails(caseDetails, children);

        assertThat(caseDetails.getData()).extracting("pageShow").isEqualTo("No");
    }

    @Test
    void shouldUpdateFinalOrderIssuedWhenAppliesToAllChildren() {
        List<Element<Child>> result = service.updateFinalOrderIssued(CARE_ORDER,
            List.of(testChild(), testChild()),"Yes", null);

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsExactly("Yes", "Yes");

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
            .containsExactly("Care order", "Care order");
    }

    @Test
    void shouldUpdateFinalOrderIssuedWhenAppliesToSelectedChildren() {
        List<Element<Child>> children = List.of(testChild(), testChild(), testChild());

        ChildSelector childSelector = ChildSelector.builder()
            .childCount("1")
            .selected(List.of(1))
            .build();

        List<Element<Child>> result = service.updateFinalOrderIssued(CARE_ORDER,
            children, "No", childSelector);

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsExactly("No", "Yes", "No");

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
            .containsExactly(null, "Care order", null);
    }

    @Test
    void shouldUpdateFinalOrderIssuedWhenAppliesToSelectedChildrenAndAlreadyIssuedForOtherChild() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued("No"),
            childWithFinalOrderIssued("Yes", CARE_ORDER.getLabel()),
            childWithFinalOrderIssued("No"), childWithFinalOrderIssued("No"));

        ChildSelector childSelector = ChildSelector.builder()
            .childCount("4")
            .selected(List.of(0, 2))
            .build();

        List<Element<Child>> result = service.updateFinalOrderIssued(CARE_ORDER,
            children, "No", childSelector);

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsExactly("Yes", "Yes", "Yes", "No");

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
            .containsExactly("Care order", "Care order", "Care order", null);
    }


    private Element<Child> childWithConfidentialFields(UUID id) {
        return element(id, Child.builder()
            .party(ChildParty.builder()
                .firstName("James")
                .detailsHidden("Yes")
                .email(EmailAddress.builder().email("email@email.com").build())
                .address(Address.builder().addressLine1("Address Line 1").build())
                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                .build())
            .build());
    }

    private Element<Child> childWithFinalOrderIssued(String finalOrderIssued) {
        return childWithFinalOrderIssued(finalOrderIssued, null);
    }

    private Element<Child> childWithFinalOrderIssued(String finalOrderIssued, String orderType) {
        return element(Child.builder()
            .finalOrderIssued(finalOrderIssued)
            .finalOrderIssuedType(orderType)
            .party(ChildParty.builder()
                .firstName(randomAlphanumeric(10))
                .lastName(randomAlphanumeric(10))
                .build())
            .build());
    }

}
