package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    void shouldPopulateCaseDataMapWithNoWhenThereIsEmptyList() {
        List<Element<Child>> children = new ArrayList<>();

        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
        service.addPageShowToCaseDetails(caseDetails, children);

        assertThat(caseDetails.getData()).extracting("pageShow").isEqualTo("No");
    }

    @Test
    void shouldUpdateFinalOrderIssuedWhenAppliesToAllChildren() {
        List<Element<Child>> result = service.updateFinalOrderIssued(List.of(testChild(), testChild()),
            "Yes", null);

        assertThat(result.get(0).getValue().getParty().getFinalOrderIssued()).isEqualTo("Yes");
        assertThat(result.get(1).getValue().getParty().getFinalOrderIssued()).isEqualTo("Yes");
    }

    @Test
    void shouldUpdateFinalOrderIssuedWhenAppliesToSelectedChildren() {
        List<Element<Child>> children = List.of(testChild(), testChild(), testChild());

        ChildSelector childSelector = ChildSelector.builder()
            .childCount("1")
            .selected(List.of(1))
            .build();

        List<Element<Child>> result = service.updateFinalOrderIssued(children, "No", childSelector);

        assertThat(result.get(0).getValue().getParty().getFinalOrderIssued()).isEqualTo("No");
        assertThat(result.get(1).getValue().getParty().getFinalOrderIssued()).isEqualTo("Yes");
        assertThat(result.get(2).getValue().getParty().getFinalOrderIssued()).isEqualTo("No");
    }

    @Test
    void shouldUpdateFinalOrderIssuedWhenAppliesToSelectedChildrenAndAlreadyIssuedForOtherChild() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued("No"), childWithFinalOrderIssued("Yes"),
            childWithFinalOrderIssued("No"));

        ChildSelector childSelector = ChildSelector.builder()
            .childCount("3")
            .selected(List.of(0))
            .build();

        List<Element<Child>> result = service.updateFinalOrderIssued(children, "No", childSelector);

        assertThat(result.get(0).getValue().getParty().getFinalOrderIssued()).isEqualTo("Yes");
        assertThat(result.get(1).getValue().getParty().getFinalOrderIssued()).isEqualTo("Yes");
        assertThat(result.get(2).getValue().getParty().getFinalOrderIssued()).isEqualTo("No");
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
        return element(Child.builder()
            .party(ChildParty.builder()
                .firstName(randomAlphanumeric(10))
                .lastName(randomAlphanumeric(10))
                .finalOrderIssued(finalOrderIssued)
                .build())
            .build());
    }
}
