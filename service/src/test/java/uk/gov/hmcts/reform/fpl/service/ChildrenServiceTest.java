//package uk.gov.hmcts.reform.fpl.service;
//
//import com.google.common.collect.ImmutableList;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
//import uk.gov.hmcts.reform.fpl.enums.PartyType;
//import uk.gov.hmcts.reform.fpl.model.Address;
//import uk.gov.hmcts.reform.fpl.model.CaseData;
//import uk.gov.hmcts.reform.fpl.model.Child;
//import uk.gov.hmcts.reform.fpl.model.ChildParty;
//import uk.gov.hmcts.reform.fpl.model.common.Element;
//import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
//import uk.gov.hmcts.reform.fpl.model.common.Telephone;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.UUID;
//
//import static java.util.Collections.emptyList;
//import static java.util.UUID.randomUUID;
//import static org.assertj.core.api.Assertions.assertThat;
//import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
//import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
//import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
//
//@ExtendWith(SpringExtension.class)
//class ChildrenServiceTest {
//    private static final UUID ID = randomUUID();
//
//    private final ChildrenService service = new ChildrenService();
//
//    @Test
//    void shouldAddPartyIDAndPartyTypeValuesToSingleChild() {
//        List<Element<Child>> children = wrapElements(Child.builder()
//            .party(ChildParty.builder()
//                .firstName("James")
//                .build())
//            .build());
//
//        CaseData caseData = CaseData.builder()
//            .children1(children)
//            .build();
//
//        List<Element<Child>> updatedChildren = service.modifyHiddenValues(caseData.getAllChildren());
//
//        assertThat(getParty(updatedChildren, 0).firstName).isEqualTo("James");
//        assertThat(getParty(updatedChildren, 0).partyType).isEqualTo(PartyType.INDIVIDUAL);
//        assertThat(getParty(updatedChildren, 0).partyId).isNotNull();
//    }
//
//    @Test
//    void shouldAddPartyIDAndPartyTypeValuesToMultipleChildren() {
//        List<Element<Child>> children = wrapElements(Child.builder()
//                .party(ChildParty.builder()
//                    .firstName("James")
//                    .build())
//                .build(),
//            Child.builder()
//                .party(ChildParty.builder()
//                    .firstName("Lucy")
//                    .build())
//                .build());
//
//        CaseData caseData = CaseData.builder()
//            .children1(children)
//            .build();
//        List<Element<Child>> updatedChildren = service.modifyHiddenValues(caseData.getAllChildren());
//
//        assertThat(getParty(updatedChildren, 0).firstName).isEqualTo("James");
//        assertThat(getParty(updatedChildren, 0).partyType).isEqualTo(PartyType.INDIVIDUAL);
//        assertThat(getParty(updatedChildren, 0).partyId).isNotNull();
//
//        assertThat(getParty(updatedChildren, 1).firstName).isEqualTo("Lucy");
//        assertThat(getParty(updatedChildren, 1).partyType).isEqualTo(PartyType.INDIVIDUAL);
//        assertThat(getParty(updatedChildren, 1).partyId).isNotNull();
//    }
//
//    @Test
//    void shouldKeepExistingPartyID() {
//        List<Element<Child>> children = wrapElements(Child.builder()
//            .party(ChildParty.builder()
//                .firstName("James")
//                .partyId("123")
//                .build())
//            .build());
//
//        CaseData caseData = CaseData.builder()
//            .children1(children)
//            .build();
//
//        List<Element<Child>> updatedChildren = service.modifyHiddenValues(caseData.getAllChildren());
//
//        assertThat(getParty(updatedChildren, 0).partyId).isEqualTo("123");
//    }
//
//    @Test
//    void shouldKeepExistingPartyIdAndContinueAddingNewPartyId() {
//        List<Element<Child>> children = wrapElements(
//            Child.builder()
//                .party(ChildParty.builder()
//                    .firstName("James")
//                    .partyId("123")
//                    .build())
//                .build(),
//            Child.builder()
//                .party(ChildParty.builder()
//                    .firstName("Lucy")
//                    .build())
//                .build());
//
//        CaseData caseData = CaseData.builder()
//            .children1(children)
//            .build();
//
//        List<Element<Child>> updatedChildren = service.modifyHiddenValues(caseData.getAllChildren());
//
//        assertThat(getParty(updatedChildren, 0).firstName).isEqualTo("James");
//        assertThat(getParty(updatedChildren, 0).partyId).isEqualTo("123");
//        assertThat(getParty(updatedChildren, 1).firstName).isEqualTo("Lucy");
//        assertThat(getParty(updatedChildren, 1).partyId).isNotNull();
//    }
//
//    @Test
//    void shouldHideChildAddressDetailsWhenConfidentialitySelected() {
//        List<Element<Child>> children = childElementWithDetailsHiddenValue("Yes");
//
//        CaseData caseData = CaseData.builder()
//            .children1(children)
//            .build();
//
//        List<Element<Child>> updatedChildren = service.modifyHiddenValues(caseData.getAllChildren());
//
//        assertThat(getParty(updatedChildren, 0).address).isNull();
//        assertThat(getParty(updatedChildren, 0).email).isNull();
//        assertThat(getParty(updatedChildren, 0).telephoneNumber).isNull();
//    }
//
//    @Test
//    void shouldNotHideChildAddressDetailsWhenConfidentialitySelected() {
//        List<Element<Child>> children = childElementWithDetailsHiddenValue("No");
//
//        CaseData caseData = CaseData.builder()
//            .children1(children)
//            .build();
//
//        List<Element<Child>> updatedChildren = service.modifyHiddenValues(caseData.getAllChildren());
//
//        assertThat(getParty(updatedChildren, 0).address).isNotNull();
//        assertThat(getParty(updatedChildren, 0).email).isNotNull();
//        assertThat(getParty(updatedChildren, 0).telephoneNumber).isNotNull();
//    }
//
//    @Test
//    void shouldBuildExpectedLabelWhenEmptyList() {
//        String label = service.getChildrenLabel(List.of());
//        assertThat(label).isEqualTo("No children in the case");
//    }
//
//    @Test
//    void shouldBuildExpectedLabelWhenPopulatedList() {
//        String label = service.getChildrenLabel(childElementWithDetailsHiddenValue(""));
//        assertThat(label).isEqualTo("Child 1: James\n");
//    }
//
//    @Test
//    void shouldPopulateCaseDataMapWithYesWhenThereAre2OrMoreChildren() {
//        List<Element<Child>> children = new ArrayList<>();
//        children.add(childWithConfidentialFields(randomUUID()));
//        children.add(childWithConfidentialFields(randomUUID()));
//
//        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
//        service.addPageShowToCaseDetails(caseDetails, children);
//
//        assertThat(caseDetails.getData()).extracting("pageShow").isEqualTo("Yes");
//    }
//
//    @Test
//    void shouldPopulateCaseDataMapWithNoWhenThereIsOneChild() {
//        List<Element<Child>> children = new ArrayList<>();
//        children.add(childWithConfidentialFields(randomUUID()));
//
//        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
//        service.addPageShowToCaseDetails(caseDetails, children);
//
//        assertThat(caseDetails.getData()).extracting("pageShow").isEqualTo("No");
//    }
//
//    @Test
//    void shouldPopulateCaseDataMapWithNoWhenThereIsEmptyList() {
//        List<Element<Child>> children = new ArrayList<>();
//
//        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
//        service.addPageShowToCaseDetails(caseDetails, children);
//
//        assertThat(caseDetails.getData()).extracting("pageShow").isEqualTo("No");
//    }
//
//    @Test
//    void shouldRemoveAllNonConfidentialFieldsWhenPopulatedChild() {
//        List<Element<Child>> children = wrapElements(populatedChild());
//
//        List<Element<Child>> confidentialChildDetails = service.retainConfidentialDetails(children);
//
//        assertThat(unwrapElements(confidentialChildDetails)).containsExactly(childWithOnlyConfidentialFields());
//    }
//
//    private Child childWithOnlyConfidentialFields() {
//        return Child.builder()
//            .party(ChildParty.builder()
//                .firstName("James")
//                .lastName("Smith")
//                .email(EmailAddress.builder().email("email@email.com").build())
//                .address(Address.builder().addressLine1("Address Line 1").build())
//                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
//                .build())
//            .build();
//    }
//
//    private Child populatedChild() {
//        return Child.builder()
//            .party(ChildParty.builder()
//                .firstName("James")
//                .lastName("Smith")
//                .detailsHidden("Yes")
//                .email(EmailAddress.builder().email("email@email.com").build())
//                .address(Address.builder().addressLine1("Address Line 1").build())
//                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
//                .additionalNeeds("Additional Needs")
//                .adoption("Adoption information")
//                .fathersName("Fathers name")
//                .gender("Male")
//                .litigationIssues("Litigation issues")
//                .build())
//            .build();
//    }
//
//    private Element<Child> childWithDetailsHiddenNo(UUID id) {
//        return element(id, Child.builder()
//            .party(ChildParty.builder()
//                .firstName("James")
//                .detailsHidden("No")
//                .email(EmailAddress.builder().email("email@email.com").build())
//                .address(Address.builder().addressLine1("Address Line 1").build())
//                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
//                .build())
//            .build());
//    }
//
//    private Element<Child> childWithRemovedConfidentialFields(UUID id) {
//        return element(id, Child.builder()
//            .party(ChildParty.builder()
//                .firstName("James")
//                .detailsHidden("Yes")
//                .build())
//            .build());
//    }
//
//    private Element<Child> childWithConfidentialFields(UUID id) {
//        return element(id, Child.builder()
//            .party(ChildParty.builder()
//                .firstName("James")
//                .detailsHidden("Yes")
//                .email(EmailAddress.builder().email("email@email.com").build())
//                .address(Address.builder().addressLine1("Address Line 1").build())
//                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
//                .build())
//            .build());
//    }
//
//    private List<Element<Child>> childElementWithDetailsHiddenValue(String hidden) {
//        return wrapElements(Child.builder()
//            .party(ChildParty.builder()
//                .firstName("James")
//                .detailsHidden(hidden)
//                .email(EmailAddress.builder().email("email@email.com").build())
//                .address(Address.builder().addressLine1("Address Line 1").build())
//                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
//                .build())
//            .build());
//    }
//
//    private ChildParty getParty(List<Element<Child>> updatedChildren, int i) {
//        return updatedChildren.get(i).getValue().getParty();
//    }
//}
