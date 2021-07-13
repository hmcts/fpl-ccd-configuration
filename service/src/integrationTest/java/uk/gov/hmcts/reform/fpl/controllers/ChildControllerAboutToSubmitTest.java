package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.ChildPolicyData;
import uk.gov.hmcts.reform.fpl.model.NoticeOfChangeChildAnswersData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerAboutToSubmitTest extends AbstractCallbackTest {

    private static final String ORGANISATION_ID = "dun dun duuuuuuuun *synthy*";
    private static final RespondentSolicitor MAIN_REPRESENTATIVE = RespondentSolicitor.builder()
        .firstName("dun dun duuuuuuuun *orchestral*")
        .lastName("dun dun duuuuuuuun *orchestral* x3")
        .organisation(Organisation.builder()
            .organisationID(ORGANISATION_ID)
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
    private static final String ORGANISATION_NAME = "Test organisation";
    private static final List<Element<Applicant>> APPLICANTS = List.of(
        element(Applicant.builder()
            .party(ApplicantParty.builder()
                .organisationName(ORGANISATION_NAME)
                .build())
            .build()));
    private static final String CHILD_NAME_1 = "John";
    private static final String CHILD_NAME_2 = "James";
    private static final String CHILD_SURNAME_1 = "Smith";
    private static final String CHILD_SURNAME_2 = "Mandy";

    ChildControllerAboutToSubmitTest() {
        super("enter-children");
    }

    @Test
    void shouldRemoveExistingRepresentativeInfoWhenMainRepresentativeIsRemoved() {

        CaseData caseDataBefore = CaseData.builder()
            .applicants(APPLICANTS)
            .children1(wrapElements(Child.builder()
                .solicitor(MAIN_REPRESENTATIVE)
                .party(ChildParty.builder().build())
                .build()))
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(MAIN_REPRESENTATIVE)
                .build())
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("No")
                .childrenMainRepresentative(MAIN_REPRESENTATIVE)
                .build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(toCallBackRequest(caseData, caseDataBefore)));

        assertThat(responseData.getAllChildren()).extracting(Element::getValue).containsExactly(
            Child.builder().party(ChildParty.builder().build()).build()
        );

        assertThat(responseData.getChildrenEventData().getChildrenMainRepresentative()).isNull();

        assertThat(responseData.getChildPolicyData()).isEqualTo(ChildPolicyData.builder()
            .childPolicy0(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORA))
            .childPolicy1(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORB))
            .childPolicy2(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORC))
            .childPolicy3(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORD))
            .childPolicy4(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORE))
            .childPolicy5(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORF))
            .childPolicy6(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORG))
            .childPolicy7(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORH))
            .childPolicy8(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORI))
            .childPolicy9(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORJ))
            .childPolicy10(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORK))
            .childPolicy11(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORL))
            .childPolicy12(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORM))
            .childPolicy13(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORN))
            .childPolicy14(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORO))
            .build());

        // TODO: HERE NOC ANSWERS SHOULD BE ALL NULL
        assertThat(responseData.getNoticeOfChangeChildAnswersData()).isEqualTo(
            NoticeOfChangeChildAnswersData.builder()
                .noticeOfChangeChildAnswers0(nocAnswers(ORGANISATION_NAME, null, null))
                .build()
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
            .applicants(APPLICANTS)
            .children1(wrapElements(
                Child.builder().party(ChildParty.builder()
                    .firstName(CHILD_NAME_1)
                    .lastName(CHILD_SURNAME_1)
                    .build()).build(),
                Child.builder().party(ChildParty.builder()
                    .firstName(CHILD_NAME_2)
                    .lastName(CHILD_SURNAME_2)
                    .build()).build()
            ))
            .childrenEventData(eventData)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getAllChildren()).extracting(Element::getValue).containsExactly(
            Child.builder().party(ChildParty.builder()
                .firstName(CHILD_NAME_1)
                .lastName(CHILD_SURNAME_1)
                .build()).solicitor(MAIN_REPRESENTATIVE).build(),
            Child.builder().party(ChildParty.builder()
                .firstName(CHILD_NAME_2)
                .lastName(CHILD_SURNAME_2)
                .build()).solicitor(MAIN_REPRESENTATIVE).build()
        );

        assertThat(responseData.getChildPolicyData()).isEqualTo(ChildPolicyData.builder()
            .childPolicy0(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORA, ORGANISATION_ID))
            .childPolicy1(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORB, ORGANISATION_ID))
            .childPolicy2(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORC))
            .childPolicy3(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORD))
            .childPolicy4(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORE))
            .childPolicy5(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORF))
            .childPolicy6(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORG))
            .childPolicy7(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORH))
            .childPolicy8(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORI))
            .childPolicy9(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORJ))
            .childPolicy10(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORK))
            .childPolicy11(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORL))
            .childPolicy12(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORM))
            .childPolicy13(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORN))
            .childPolicy14(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORO))
            .build());

        // TODO: HERE NOC ANSWERS SHOULD BE ALL NULL
        assertThat(responseData.getNoticeOfChangeChildAnswersData()).isEqualTo(
            NoticeOfChangeChildAnswersData.builder()
                .noticeOfChangeChildAnswers0(nocAnswers(ORGANISATION_NAME, CHILD_NAME_1, CHILD_SURNAME_1))
                .noticeOfChangeChildAnswers1(nocAnswers(ORGANISATION_NAME, CHILD_NAME_2, CHILD_SURNAME_2))
                .build()
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
            .applicants(APPLICANTS)
            .children1(wrapElements(
                Child.builder().party(ChildParty.builder().build()).build(),
                Child.builder().party(ChildParty.builder().build()).build()
            ))
            .childrenEventData(eventData)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getAllChildren()).extracting(Element::getValue).containsExactly(
            Child.builder().party(ChildParty.builder().build()).solicitor(MAIN_REPRESENTATIVE).build(),
            Child.builder().party(ChildParty.builder().build()).solicitor(ANOTHER_REPRESENTATIVE).build()
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
            .applicants(APPLICANTS)
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

    private OrganisationPolicy buildOrganisationPolicy(SolicitorRole solicitorRole) {
        return buildOrganisationPolicy(solicitorRole, null);
    }

    private OrganisationPolicy buildOrganisationPolicy(SolicitorRole solicitorRole, String orgId) {
        return OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID(orgId).build())
            .orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel())
            .build();
    }

    private NoticeOfChangeAnswers nocAnswers(String organisationName,
                                             String respondentFirstName,
                                             String respondentLastName) {
        return NoticeOfChangeAnswers.builder()
            .applicantName(organisationName)
            .respondentFirstName(respondentFirstName)
            .respondentLastName(respondentLastName)
            .build();
    }

}
