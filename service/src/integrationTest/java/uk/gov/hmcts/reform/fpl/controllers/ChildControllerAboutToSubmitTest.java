package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.ChildPolicyData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.NoticeOfChangeChildAnswersData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentation;
import uk.gov.hmcts.reform.fpl.model.noc.ChangedRepresentative;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.RespondentAfterSubmissionRepresentationService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerAboutToSubmitTest extends AbstractCallbackTest {

    private static final State NON_RESTRICTED_STATE = State.SUBMITTED;
    private static final String ORGANISATION_ID = "dun dun duuuuuuuun *synthy*";
    private static final String ANOTHER_ORGANISATION_ID = "whistling tune that sounds like";
    private static final String MAIN_SOLICITOR_FIRST_NAME = "dun dun duuuuuuuun *orchestral*";
    private static final String MAIN_SOLICITOR_LAST_NAME = "dun dun duuuuuuuun *orchestral* x3";
    private static final String MAIN_SOLICITOR_EMAIL = "emaial* x3";
    private static final RespondentSolicitor MAIN_REPRESENTATIVE = RespondentSolicitor.builder()
        .firstName(MAIN_SOLICITOR_FIRST_NAME)
        .lastName(MAIN_SOLICITOR_LAST_NAME)
        .email(MAIN_SOLICITOR_EMAIL)
        .organisation(Organisation.builder()
            .organisationID(ORGANISATION_ID)
            .build())
        .build();
    private static final String ANOTHER_SOLICITOR_FIRST_NAME = "dun dun duuuun *now with synth and orchestra*";
    private static final String ANOTHER_SOLICITOR_LAST_NAME = "dun dun dun, dun dun dun *quickly*";
    private static final String ANOTHER_SOLICITOR_EMAIL = "email*";
    private static final RespondentSolicitor ANOTHER_REPRESENTATIVE = RespondentSolicitor.builder()
        .firstName(ANOTHER_SOLICITOR_FIRST_NAME)
        .lastName(ANOTHER_SOLICITOR_LAST_NAME)
        .email(ANOTHER_SOLICITOR_EMAIL)
        .organisation(Organisation.builder()
            .organisationID(ANOTHER_ORGANISATION_ID)
            .build())
        .build();
    private static final String ORGANISATION_NAME = "Test organisation";
    private static final List<Element<LocalAuthority>> LOCAL_AUTHORITIES = List.of(
        element(LocalAuthority.builder().name(ORGANISATION_NAME).build())
    );
    private static final String CHILD_NAME_1 = "John";
    private static final String CHILD_NAME_2 = "James";
    private static final String CHILD_SURNAME_1 = "Smith";
    private static final String CHILD_SURNAME_2 = "Mandy";
    private static final UUID UUID_1 = UUID.randomUUID();
    private static final UUID UUID_2 = UUID.randomUUID();

    @MockBean
    private IdentityService identityService;

    @MockBean
    private FeatureToggleService toggleService;

    @SpyBean
    private RespondentAfterSubmissionRepresentationService representationService;

    ChildControllerAboutToSubmitTest() {
        super("enter-children");
    }

    @BeforeEach
    void setUp() {
        when(toggleService.isChildRepresentativeSolicitorEnabled()).thenReturn(true);
    }

    @Test
    void shouldRetainEmptyPolicyDataWhenChildrenDoNotHaveMainRepresentative() {
        Child child = Child.builder()
            .solicitor(null)
            .party(ChildParty.builder().build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .localAuthorities(LOCAL_AUTHORITIES)
            .children1(wrapElements(child))
            .childPolicyData(basePolicyData().build())
            .noticeOfChangeChildAnswersData(NoticeOfChangeChildAnswersData.builder()
                .noticeOfChangeChildAnswers0(nocAnswers(null, null, null))
                .build())
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .state(NON_RESTRICTED_STATE)
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("No")
                .build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(toCallBackRequest(caseData, caseDataBefore)));

        assertThat(responseData.getAllChildren()).extracting(Element::getValue).containsExactly(child);

        assertThat(responseData.getChildPolicyData()).isEqualTo(basePolicyData().build());

        assertThat(responseData.getNoticeOfChangeChildAnswersData()).isEqualTo(
            NoticeOfChangeChildAnswersData.builder()
                .noticeOfChangeChildAnswers0(nocAnswers(null, null, null))
                .build()
        );
    }

    @Test
    void shouldDoNothingIfOpenState() {
        CaseData caseData = CaseData.builder()
            .state(State.OPEN)
            .localAuthorities(LOCAL_AUTHORITIES)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().build())
                .build()))
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("No")
                .build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getAllChildren()).extracting(Element::getValue).containsExactly(
            Child.builder().party(ChildParty.builder().build()).build()
        );

        assertThat(responseData.getChildrenEventData()).isEqualTo(ChildrenEventData.builder()
            .childrenHaveRepresentation("No")
            .build());

        assertThat(responseData.getChildPolicyData()).isEqualTo(ChildPolicyData.builder().build());

        assertThat(responseData.getNoticeOfChangeChildAnswersData()).isEqualTo(
            NoticeOfChangeChildAnswersData.builder().build()
        );
    }

    @Test
    void shouldAddMainRepresentativeInfoWhenAllUseMainRepresentativeIsSelectedForTheFirstTime() {
        CaseData caseDataBefore = CaseData.builder()
            .localAuthorities(LOCAL_AUTHORITIES)
            .children1(wrapElements(
                Child.builder()
                    .party(ChildParty.builder().firstName(CHILD_NAME_1).lastName(CHILD_SURNAME_1).build())
                    .build(),
                Child.builder()
                    .party(ChildParty.builder().firstName(CHILD_NAME_2).lastName(CHILD_SURNAME_2).build())
                    .build()
            )).build();

        CaseData caseData = caseDataBefore.toBuilder()
            .state(NON_RESTRICTED_STATE)
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(MAIN_REPRESENTATIVE)
                .childrenHaveSameRepresentation("Yes")
                .build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(toCallBackRequest(caseData, caseDataBefore)));

        assertThat(responseData.getAllChildren()).extracting(Element::getValue).containsExactly(
            Child.builder()
                .party(ChildParty.builder().firstName(CHILD_NAME_1).lastName(CHILD_SURNAME_1).build())
                .solicitor(MAIN_REPRESENTATIVE)
                .build(),
            Child.builder()
                .party(ChildParty.builder().firstName(CHILD_NAME_2).lastName(CHILD_SURNAME_2).build())
                .solicitor(MAIN_REPRESENTATIVE)
                .build()
        );

        assertThat(responseData.getChildPolicyData()).isEqualTo(
            basePolicyData()
                .childPolicy0(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORA, ORGANISATION_ID))
                .childPolicy1(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORB, ORGANISATION_ID))
                .build()
        );

        assertThat(responseData.getNoticeOfChangeChildAnswersData()).isEqualTo(
            NoticeOfChangeChildAnswersData.builder()
                .noticeOfChangeChildAnswers0(nocAnswers(ORGANISATION_NAME, CHILD_NAME_1, CHILD_SURNAME_1))
                .noticeOfChangeChildAnswers1(nocAnswers(ORGANISATION_NAME, CHILD_NAME_2, CHILD_SURNAME_2))
                .build()
        );

        assertThat(responseData.getChangeOfRepresentatives()).isNull();

    }

    @Test
    void shouldAddMainRepresentativeInfoWhenAllUseMainRepresentativeIfBeforeNotSelected() {
        CaseData caseDataBefore = CaseData.builder()
            .localAuthorities(LOCAL_AUTHORITIES)
            .children1(wrapElements(
                Child.builder()
                    .party(ChildParty.builder().firstName(CHILD_NAME_1).lastName(CHILD_SURNAME_1).build())
                    .build(),
                Child.builder()
                    .party(ChildParty.builder().firstName(CHILD_NAME_2).lastName(CHILD_SURNAME_2).build())
                    .build()
            )).childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("No")
                .build())
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .state(NON_RESTRICTED_STATE)
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(MAIN_REPRESENTATIVE)
                .childrenHaveSameRepresentation("Yes")
                .build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(toCallBackRequest(caseData, caseDataBefore)));

        assertThat(responseData.getAllChildren()).extracting(Element::getValue).containsExactly(
            Child.builder()
                .party(ChildParty.builder().firstName(CHILD_NAME_1).lastName(CHILD_SURNAME_1).build())
                .solicitor(MAIN_REPRESENTATIVE)
                .build(),
            Child.builder()
                .party(ChildParty.builder().firstName(CHILD_NAME_2).lastName(CHILD_SURNAME_2).build())
                .solicitor(MAIN_REPRESENTATIVE)
                .build()
        );

        assertThat(responseData.getChildPolicyData()).isEqualTo(
            basePolicyData()
                .childPolicy0(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORA, ORGANISATION_ID))
                .childPolicy1(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORB, ORGANISATION_ID))
                .build()
        );

        assertThat(responseData.getNoticeOfChangeChildAnswersData()).isEqualTo(
            NoticeOfChangeChildAnswersData.builder()
                .noticeOfChangeChildAnswers0(nocAnswers(ORGANISATION_NAME, CHILD_NAME_1, CHILD_SURNAME_1))
                .noticeOfChangeChildAnswers1(nocAnswers(ORGANISATION_NAME, CHILD_NAME_2, CHILD_SURNAME_2))
                .build()
        );

        assertThat(responseData.getChangeOfRepresentatives()).isNull();
    }

    @Test
    void shouldChangeMainRepresentativeInfoWhenPreviousOneWasPresent() {
        when(identityService.generateId()).thenReturn(UUID_1, UUID_2);

        CaseData caseDataBefore = CaseData.builder()
            .localAuthorities(LOCAL_AUTHORITIES)
            .children1(wrapElements(
                Child.builder()
                    .party(ChildParty.builder().firstName(CHILD_NAME_1).lastName(CHILD_SURNAME_1).build())
                    .solicitor(MAIN_REPRESENTATIVE)
                    .build(),
                Child.builder()
                    .party(ChildParty.builder().firstName(CHILD_NAME_2).lastName(CHILD_SURNAME_2).build())
                    .solicitor(MAIN_REPRESENTATIVE)
                    .build()
            )).childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(MAIN_REPRESENTATIVE)
                .childrenHaveSameRepresentation("Yes")
                .build())
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .state(NON_RESTRICTED_STATE)
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(ANOTHER_REPRESENTATIVE)
                .childrenHaveSameRepresentation("Yes")
                .build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(toCallBackRequest(caseData, caseDataBefore)));

        assertThat(responseData.getAllChildren()).extracting(Element::getValue).containsExactly(
            Child.builder()
                .party(ChildParty.builder().firstName(CHILD_NAME_1).lastName(CHILD_SURNAME_1).build())
                .solicitor(ANOTHER_REPRESENTATIVE)
                .build(),
            Child.builder()
                .party(ChildParty.builder().firstName(CHILD_NAME_2).lastName(CHILD_SURNAME_2).build())
                .solicitor(ANOTHER_REPRESENTATIVE)
                .build()
        );

        assertThat(responseData.getChildPolicyData()).isEqualTo(
            basePolicyData()
                .childPolicy0(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORA, ANOTHER_ORGANISATION_ID))
                .childPolicy1(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORB, ANOTHER_ORGANISATION_ID))
                .build()
        );

        assertThat(responseData.getNoticeOfChangeChildAnswersData()).isEqualTo(
            NoticeOfChangeChildAnswersData.builder()
                .noticeOfChangeChildAnswers0(nocAnswers(ORGANISATION_NAME, CHILD_NAME_1, CHILD_SURNAME_1))
                .noticeOfChangeChildAnswers1(nocAnswers(ORGANISATION_NAME, CHILD_NAME_2, CHILD_SURNAME_2))
                .build()
        );

        assertThat(responseData.getChangeOfRepresentatives()).isEqualTo(List.of(
            element(UUID_1, ChangeOfRepresentation.builder()
                .child(String.format("%s %s", CHILD_NAME_1, CHILD_SURNAME_1))
                .date(dateNow())
                .by("HMCTS")
                .via("FPL")
                .added(ChangedRepresentative.builder()
                    .firstName(ANOTHER_SOLICITOR_FIRST_NAME)
                    .lastName(ANOTHER_SOLICITOR_LAST_NAME)
                    .organisation(Organisation.builder()
                        .organisationID(ANOTHER_ORGANISATION_ID)
                        .build())
                    .email(ANOTHER_SOLICITOR_EMAIL)
                    .build())
                .removed(ChangedRepresentative.builder()
                    .firstName(MAIN_SOLICITOR_FIRST_NAME)
                    .lastName(MAIN_SOLICITOR_LAST_NAME)
                    .organisation(Organisation.builder()
                        .organisationID(ORGANISATION_ID)
                        .build())
                    .email(MAIN_SOLICITOR_EMAIL)
                    .build())
                .build()),
            element(UUID_2, ChangeOfRepresentation.builder()
                .child(String.format("%s %s", CHILD_NAME_2, CHILD_SURNAME_2))
                .date(dateNow())
                .by("HMCTS")
                .via("FPL")
                .added(ChangedRepresentative.builder()
                    .firstName(ANOTHER_SOLICITOR_FIRST_NAME)
                    .lastName(ANOTHER_SOLICITOR_LAST_NAME)
                    .organisation(Organisation.builder()
                        .organisationID(ANOTHER_ORGANISATION_ID)
                        .build())
                    .email(ANOTHER_SOLICITOR_EMAIL)
                    .build())
                .removed(ChangedRepresentative.builder()
                    .firstName(MAIN_SOLICITOR_FIRST_NAME)
                    .lastName(MAIN_SOLICITOR_LAST_NAME)
                    .organisation(Organisation.builder()
                        .organisationID(ORGANISATION_ID)
                        .build())
                    .email(MAIN_SOLICITOR_EMAIL)
                    .build())
                .build())
        ));
    }

    @Test
    void shouldNotInteractWithRepresentativeServiceWhenToggleIsOff() {
        when(toggleService.isChildRepresentativeSolicitorEnabled()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .children1(wrapElements(
                Child.builder()
                    .party(ChildParty.builder().firstName(CHILD_NAME_1).lastName(CHILD_SURNAME_1).build())
                    .solicitor(MAIN_REPRESENTATIVE)
                    .build(),
                Child.builder()
                    .party(ChildParty.builder().firstName(CHILD_NAME_2).lastName(CHILD_SURNAME_2).build())
                    .solicitor(MAIN_REPRESENTATIVE)
                    .build()
            )).build();

        postAboutToSubmitEvent(caseData);

        verifyNoInteractions(representationService);
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
            .state(NON_RESTRICTED_STATE)
            .localAuthorities(LOCAL_AUTHORITIES)
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
            .state(NON_RESTRICTED_STATE)
            .localAuthorities(LOCAL_AUTHORITIES)
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

    private ChildPolicyData.ChildPolicyDataBuilder basePolicyData() {
        return ChildPolicyData.builder()
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
            .childPolicy14(buildOrganisationPolicy(SolicitorRole.CHILDSOLICITORO));
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

    private NoticeOfChangeAnswers nocAnswers(String organisationName, String respondentFirstName,
                                             String respondentLastName) {
        return NoticeOfChangeAnswers.builder()
            .applicantName(organisationName)
            .respondentFirstName(respondentFirstName)
            .respondentLastName(respondentLastName)
            .build();
    }

}
