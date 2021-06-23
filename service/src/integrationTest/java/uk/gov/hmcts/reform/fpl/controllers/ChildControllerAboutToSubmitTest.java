package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerAboutToSubmitTest extends AbstractCallbackTest {

    private static final RespondentSolicitor MAIN_REPRESENTATIVE = RespondentSolicitor.builder()
        .firstName("dun dun duuuuuuuun *orchestral*")
        .lastName("dun dun duuuuuuuun *orchestral* x3")
        .organisation(Organisation.builder()
            .organisationID("dun dun duuuuuuuun *synthy*")
            .build())
        .build();
    private static final RespondentSolicitor ANOTHER_REPRESENTATIVE = RespondentSolicitor.builder()
        .firstName("dun dun duuuun *now with synth and orchestra*")
        .lastName("dun dun dun, dun dun dun *quickly*")
        .organisation(Organisation.builder()
            .organisationID("whistling tune that sounds like")
            .organisationName("\"The chances of anything coming from Mars are a million to one, he said\"")
            .build())
        .build();

    ChildControllerAboutToSubmitTest() {
        super("enter-children");
    }

    @Test
    void shouldRemoveExistingRepresentativeInfoWhenMainRepresentativeIsRemoved() {
        ChildrenEventData eventData = ChildrenEventData.builder()
            .childrenHaveRepresentation("No")
            .build();

        CaseData caseData = CaseData.builder()
            .children1(wrapElements(Child.builder()
                .representative(MAIN_REPRESENTATIVE)
                .party(ChildParty.builder().build())
                .build()))
            .childrenEventData(eventData)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getAllChildren()).extracting(Element::getValue).containsExactly(
            Child.builder().party(ChildParty.builder().build()).build()
        );
    }

    @Test
    void shouldAddMainRepresentativeInfoWhenAllUseMainRepresentativeIsSelected() {
        ChildrenEventData eventData = ChildrenEventData.builder()
            .childrenHaveRepresentation("Yes")
            .childrenMainRepresentative(MAIN_REPRESENTATIVE)
            .childrenHaveSameRepresentation("Yes")
            .build();

        CaseData caseData = CaseData.builder()
            .children1(wrapElements(
                Child.builder().party(ChildParty.builder().build()).build(),
                Child.builder().party(ChildParty.builder().build()).build()
            ))
            .childrenEventData(eventData)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getAllChildren()).extracting(Element::getValue).containsExactly(
            Child.builder().party(ChildParty.builder().build()).representative(MAIN_REPRESENTATIVE).build(),
            Child.builder().party(ChildParty.builder().build()).representative(MAIN_REPRESENTATIVE).build()
        );
    }

    @Test
    void shouldAddSelectedRepresentative() {
        ChildrenEventData eventData = ChildrenEventData.builder()
            .childrenHaveRepresentation("Yes")
            .childrenMainRepresentative(MAIN_REPRESENTATIVE)
            .childrenHaveSameRepresentation("No")
            .childRepresentationDetails0(ChildRepresentationDetails.builder()
                .useMainSolicitor("Yes")
                .build())
            .childRepresentationDetails1(ChildRepresentationDetails.builder()
                .useMainSolicitor("No")
                .solicitor(ANOTHER_REPRESENTATIVE)
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .children1(wrapElements(
                Child.builder().party(ChildParty.builder().build()).build(),
                Child.builder().party(ChildParty.builder().build()).build()
            ))
            .childrenEventData(eventData)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getAllChildren()).extracting(Element::getValue).containsExactly(
            Child.builder().party(ChildParty.builder().build()).representative(MAIN_REPRESENTATIVE).build(),
            Child.builder().party(ChildParty.builder().build()).representative(ANOTHER_REPRESENTATIVE).build()
        );
    }

    @Test
    void shouldAddConfidentialChildrenToCaseDataWhenConfidentialChildrenExist() {
        ChildParty confidentialParty = ChildParty.builder()
            .firstName("Phil")
            .lastName("Lynott")
            .address(Address.builder()
                .addressLine1("Horsell Common")
                .addressLine2("Shores Road")
                .addressLine3("Woking")
                .postTown("GU21 4XB")
                .build())
            .telephoneNumber(Telephone.builder().telephoneNumber("12345").build())
            .detailsHidden("Yes")
            .build();

        ChildParty nonConfidentialParty = confidentialParty.toBuilder().detailsHidden("No").build();

        UUID confidentialChildID = UUID.randomUUID();
        CaseData initialCaseData = CaseData.builder()
            .children1(List.of(
                element(confidentialChildID, Child.builder().party(confidentialParty).build()),
                element(Child.builder().party(nonConfidentialParty).build())
            ))
            .build();


        CaseData caseData = extractCaseData(postAboutToSubmitEvent(initialCaseData));

        ChildParty updatedConfidentialParty = confidentialParty.toBuilder()
            .showAddressInConfidentialTab("Yes")
            .detailsHidden(null)
            .build();

        assertThat(caseData.getConfidentialChildren())
            .containsOnly(element(confidentialChildID, Child.builder().party(updatedConfidentialParty).build()));

        assertThat(caseData.getChildren1()).extracting(child -> child.getValue().getParty()).containsExactly(
            confidentialParty.toBuilder().address(null).telephoneNumber(null).build(),
            nonConfidentialParty
        );
    }
}
