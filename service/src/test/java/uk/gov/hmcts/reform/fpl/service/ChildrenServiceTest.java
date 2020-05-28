package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;

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
    void shouldBuildExpectedLabelWhenPopulatedListAndFinalOrderIssuedOnChild() {
        List<Element<Child>> children = List.of(childWithConfidentialFields(randomUUID()),
            childWithFinalOrderIssued("Jack","Hill"));
        String label = service.getChildrenLabel(children);
        assertThat(label).isEqualTo("Child 1: James\nChild 2: Jack Hill - Care order issued\n");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnFalseWhenListEmptyOrNull(List<Element<Child>> list) {
        boolean result = service.allChildrenHaveFinalOrder(list);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAtLeastOneChildDoesNotHaveFinalOrder() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssuedYes(), childWithFinalOrderIssuedNo());

        boolean result = service.allChildrenHaveFinalOrder(children);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAllChildrenHaveFinalOrder() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssuedYes(), childWithFinalOrderIssuedYes());

        boolean result = service.allChildrenHaveFinalOrder(children);

        assertThat(result).isTrue();
    }

    @Test
    void shouldUpdateFinalOrderIssuedWhenAppliesToAllChildren() {
        List<Element<Child>> result = service.updateFinalOrderIssued(CARE_ORDER, testChildren(),"Yes", null, null);

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsExactly("Yes", "Yes", "Yes");

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
            .containsExactly("Care order", "Care order", "Care order");
    }

    @Test
    void shouldUpdateFinalOrderIssuedWhenAppliesToSelectedChildren() {
        List<Element<Child>> children = testChildren();

        ChildSelector childSelector = ChildSelector.builder()
            .childCount("1")
            .selected(List.of(1))
            .build();

        List<Element<Child>> result = service.updateFinalOrderIssued(CARE_ORDER,
            children, "No", childSelector, null);

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsExactly("No", "Yes", "No");

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
            .containsExactly(null, "Care order", null);
    }

    @Test
    void shouldUpdateFinalOrderIssuedWhenAppliesToSelectedChildrenAndAlreadyIssuedForOtherChildren() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssuedNo(),
            childWithFinalOrderIssuedYes(),
            childWithFinalOrderIssuedNo(), childWithFinalOrderIssuedNo(), childWithFinalOrderIssuedNo());

        ChildSelector childSelector = ChildSelector.builder()
            .childCount("5")
            .selected(List.of(0, 2))
            .build();

        List<Element<Child>> result = service.updateFinalOrderIssued(CARE_ORDER,
            children, "No", childSelector, null);

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsExactly("Yes", "Yes", "Yes", "No", "No");

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
            .containsExactly("Care order", "Care order", "Care order", null, null);
    }

    @Test
    void shouldUpdateFinalOrderIssuedWhenAppliesToSelectedChildrenAndOneRemainingChild() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssuedYes(),
            childWithFinalOrderIssuedNo(),
            childWithFinalOrderIssuedYes());

        List<Element<Child>> result = service.updateFinalOrderIssued(SUPERVISION_ORDER,
            children, "No", null, "1");

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsExactly("Yes", "Yes", "Yes");

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
            .containsExactly("Care order", "Supervision order", "Care order");
    }

    @Test
    void shouldGetRemainingChildIndexWhenOneRemainingChild() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssuedYes(),
            childWithFinalOrderIssuedNo(),
            childWithFinalOrderIssuedYes());

        Optional<Integer> result = service.getRemainingChildIndex(children);

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(1);
    }

    @Test
    void shouldNotGetRemainingChildIndexWhenNoRemainingChild() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssuedYes(),
            childWithFinalOrderIssuedYes());

        Optional<Integer> result = service.getRemainingChildIndex(children);

        assertThat(result.isPresent()).isFalse();
    }

    @Test
    void shouldNotGetRemainingChildIndexWhenMoreThanOneRemainingChild() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssuedYes(), childWithFinalOrderIssuedNo(),
            childWithFinalOrderIssuedYes(), childWithFinalOrderIssuedNo());

        Optional<Integer> result = service.getRemainingChildIndex(children);

        assertThat(result.isPresent()).isFalse();
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

    private Element<Child> childWithFinalOrderIssuedYes() {
        return childWithFinalOrderIssued(randomAlphanumeric(10), randomAlphanumeric(10),
            YES.getValue(), CARE_ORDER.getLabel());
    }

    private Element<Child> childWithFinalOrderIssuedNo() {
        return childWithFinalOrderIssued(randomAlphanumeric(10), randomAlphanumeric(10),
            NO.getValue(), null);
    }

    private Element<Child> childWithFinalOrderIssued(String firstName, String lastName) {
        return childWithFinalOrderIssued(firstName, lastName, YES.getValue(), CARE_ORDER.getLabel());
    }

    private Element<Child> childWithFinalOrderIssued(String firstName, String lastName,
        String finalOrderIssued, String orderType) {
        return element(Child.builder()
            .party(ChildParty.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build())
            .finalOrderIssued(finalOrderIssued)
            .finalOrderIssuedType(orderType)
            .build());
    }

}
