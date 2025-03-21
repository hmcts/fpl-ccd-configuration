package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.aac.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.aac.model.DecisionRequest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.AuditEvent;
import uk.gov.hmcts.reform.ccd.model.AuditEventsResponse;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentation;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentationMethod;
import uk.gov.hmcts.reform.fpl.model.noc.ChangedRepresentative;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.rd.model.ContactInformation;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.Organisation.organisation;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@WebMvcTest(NoticeOfChangeController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfChangeAboutToStartControllerTest extends AbstractCallbackTest {

    private static final Long CASE_ID = 10L;
    private static final String SOLICITOR_ID = "1111111";
    private static final LocalDate TODAY = LocalDate.now();

    private static final Organisation NEW_ORGANISATION = organisation("NEW_ORG");
    private static final UUID NEW_CHANGE_UUID = UUID.randomUUID();
    private static final String RESPONDENT_FIRST_NAME = "First";
    private static final String RESPONDENT_LAST_NAME = "Respondent";
    private static final String OLD_SOLICITOR_FIRST_NAME = "Tim";
    private static final String OLD_SOLICITOR_LAST_NAME = "Brown";
    private static final Organisation OLD_ORGANISATION = organisation("OLD_ORG");
    private static final String OLD_SOLICITOR_EMAIL = "tim.brown@test.com";
    private static final AuditEventsResponse AUDIT_EVENTS = AuditEventsResponse.builder()
        .auditEvents(List.of(AuditEvent.builder()
            .userId(SOLICITOR_ID)
            .id("nocRequest")
            .build()))
        .build();
    private static final String SOLICITOR_USER_EMAIL = "emma.willson@test.com";
    private static final String SOLICITOR_FIRST_NAME = "Emma";
    private static final String SOLICITOR_LAST_NAME = "Willson";
    private static final UserDetails SOLICITOR_USER = UserDetails.builder()
        .forename(SOLICITOR_FIRST_NAME)
        .surname(SOLICITOR_LAST_NAME)
        .email(SOLICITOR_USER_EMAIL)
        .id(SOLICITOR_ID)
        .build();

    private static final AboutToStartOrSubmitCallbackResponse ASSIGNMENT_RESPONSE = AboutToStartOrSubmitCallbackResponse
        .builder()
        .build();
    private static final String SECOND_RESPONDENT_FIRST_NAME = "Second";
    private static final String SECOND_RESPONDENT_LAST_NAME = "Respondent";

    @MockBean
    private IdentityService identityService;

    @MockBean
    private Time time;

    @Captor
    private ArgumentCaptor<DecisionRequest> requestCaptor;

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @MockBean
    private OrganisationService organisationService;

    NoticeOfChangeAboutToStartControllerTest() {
        super("noc-decision");
    }

    @BeforeEach
    void init() {
        givenFplService();
        givenSystemUser();
        when(coreCaseDataApi.getAuditEvents(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, false, CASE_ID.toString()))
            .thenReturn(AUDIT_EVENTS);
        when(idamClient.getUserByUserId(USER_AUTH_TOKEN, SOLICITOR_ID))
            .thenReturn(SOLICITOR_USER);
        when(caseAssignmentApi.applyDecision(eq(USER_AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), requestCaptor.capture()))
            .thenReturn(ASSIGNMENT_RESPONSE);
        when(identityService.generateId()).thenReturn(NEW_CHANGE_UUID);
        when(time.now()).thenReturn(TODAY.atStartOfDay());
    }

    @Test
    void shouldAddRespondentRepresentation() {

        final Element<Respondent> respondent1 = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("First")
                .lastName("Respondent")
                .build())
            .build());

        final Element<Respondent> respondent2 = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName(SECOND_RESPONDENT_FIRST_NAME)
                .lastName(SECOND_RESPONDENT_LAST_NAME)
                .build())
            .build());

        final Element<Respondent> respondent3 = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Third")
                .lastName("Respondent")
                .build())
            .build());

        final ChangeOrganisationRequest changeRequest = ChangeOrganisationRequest.builder()
            .organisationToAdd(NEW_ORGANISATION)
            .caseRoleId(caseRoleDynamicList("[SOLICITORB]"))
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(List.of(respondent1, respondent2, respondent3))
            .changeOrganisationRequestField(changeRequest)
            .build();

        final AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToStartEvent(caseData);

        final CaseData updatedCaseData = extractCaseData(requestCaptor.getValue().getCaseDetails());

        final Element<Respondent> expectedRespondent = update(respondent2, SOLICITOR_USER, NEW_ORGANISATION);

        assertThat(actualResponse).isEqualTo(ASSIGNMENT_RESPONSE);
        assertThat(updatedCaseData.getRespondents1()).containsExactly(respondent1, expectedRespondent, respondent3);
        assertThat(updatedCaseData.getChangeOfRepresentatives()).isEqualTo(List.of(element(NEW_CHANGE_UUID,
            ChangeOfRepresentation.builder()
                .respondent(SECOND_RESPONDENT_FIRST_NAME + " " + SECOND_RESPONDENT_LAST_NAME)
                .via(ChangeOfRepresentationMethod.NOC.getLabel())
                .by(SOLICITOR_USER_EMAIL)
                .date(TODAY)
                .removed(null)
                .added(ChangedRepresentative.builder()
                    .email(SOLICITOR_USER_EMAIL)
                    .firstName(SOLICITOR_FIRST_NAME)
                    .lastName(SOLICITOR_LAST_NAME)
                    .organisation(NEW_ORGANISATION)
                    .build())
                .build())
        ));
    }

    @Test
    void shouldUpdateRespondentRepresentation() {

        final Element<Respondent> respondent1 = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName(RESPONDENT_FIRST_NAME)
                .lastName(RESPONDENT_LAST_NAME)
                .build())
            .solicitor(RespondentSolicitor.builder()
                .firstName(OLD_SOLICITOR_FIRST_NAME)
                .lastName(OLD_SOLICITOR_LAST_NAME)
                .email(OLD_SOLICITOR_EMAIL)
                .organisation(OLD_ORGANISATION)
                .build())
            .build());

        final Element<Respondent> respondent2 = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Second")
                .lastName("Respondent")
                .build())
            .build());

        final Element<Respondent> respondent3 = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Third")
                .lastName("Respondent")
                .build())
            .build());

        final ChangeOrganisationRequest changeRequest = ChangeOrganisationRequest.builder()
            .organisationToAdd(NEW_ORGANISATION)
            .caseRoleId(caseRoleDynamicList("[SOLICITORA]"))
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .changeOrganisationRequestField(changeRequest)
            .respondents1(List.of(respondent1, respondent2, respondent3))
            .build();


        final AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToStartEvent(caseData);

        final CaseData updatedCaseData = extractCaseData(requestCaptor.getValue().getCaseDetails());

        final Element<Respondent> expectedRespondent = update(respondent1, SOLICITOR_USER, NEW_ORGANISATION);

        assertThat(updatedCaseData.getRespondents1()).containsExactly(expectedRespondent, respondent2, respondent3);
        assertThat(updatedCaseData.getChangeOfRepresentatives()).isEqualTo(List.of(element(NEW_CHANGE_UUID,
            ChangeOfRepresentation.builder()
                .respondent(RESPONDENT_FIRST_NAME + " " + RESPONDENT_LAST_NAME)
                .via(ChangeOfRepresentationMethod.NOC.getLabel())
                .by(SOLICITOR_USER_EMAIL)
                .date(TODAY)
                .removed(ChangedRepresentative.builder()
                    .email(OLD_SOLICITOR_EMAIL)
                    .firstName(OLD_SOLICITOR_FIRST_NAME)
                    .lastName(OLD_SOLICITOR_LAST_NAME)
                    .organisation(OLD_ORGANISATION)
                    .build())
                .added(ChangedRepresentative.builder()
                    .email(SOLICITOR_USER_EMAIL)
                    .firstName(SOLICITOR_FIRST_NAME)
                    .lastName(SOLICITOR_LAST_NAME)
                    .organisation(NEW_ORGANISATION)
                    .build())
                .build())
        ));
        assertThat(actualResponse).isEqualTo(ASSIGNMENT_RESPONSE);
    }

    @Test
    void shouldUpdateSolicitorForThirdPartyApplication() {
        when(organisationService.getOrganisation("NEW_ORG")).thenReturn(
            uk.gov.hmcts.reform.rd.model.Organisation.builder()
                .organisationIdentifier("NEW_ORG")
                .contactInformation(List.of(ContactInformation.builder()
                       .addressLine1("New Test Road")
                   .build()))
            .build());

        final ChangeOrganisationRequest changeRequest = ChangeOrganisationRequest.builder()
            .organisationToAdd(NEW_ORGANISATION)
            .caseRoleId(caseRoleDynamicList("[EPSMANAGING]"))
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .changeOrganisationRequestField(changeRequest)
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(OLD_ORGANISATION)
                .orgPolicyCaseAssignedRole("[EPSMANAGING]")
                .build())
            .localAuthorities(List.of(element(LocalAuthority.builder()
                .id("ORG123")
                .name("Joe Bloggs")
                .email("test1@testmail.com")
                .phone("111222333")
                .address(Address.builder()
                    .addressLine1("Old Test Road")
                    .build())
                .build())))
            .build();

        final AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToStartEvent(caseData);

        final CaseData updatedCaseData = extractCaseData(requestCaptor.getValue().getCaseDetails());

        final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
            .id(NEW_ORGANISATION.getOrganisationID())
            .name("Joe Bloggs")
            .address(Address.builder()
                .addressLine1("New Test Road")
                .build())
            .build();

        assertThat(updatedCaseData.getLocalAuthorities().get(0).getValue()).isEqualTo(expectedLocalAuthority);
        assertThat(updatedCaseData.getChangeOfRepresentatives()).isEqualTo(List.of(element(NEW_CHANGE_UUID,
            ChangeOfRepresentation.builder()
                .via(ChangeOfRepresentationMethod.NOC.getLabel())
                .by(SOLICITOR_USER_EMAIL)
                .date(TODAY)
                .removed(ChangedRepresentative.builder()
                    .organisation(OLD_ORGANISATION)
                    .build())
                .added(ChangedRepresentative.builder()
                    .email(SOLICITOR_USER_EMAIL)
                    .firstName(SOLICITOR_FIRST_NAME)
                    .lastName(SOLICITOR_LAST_NAME)
                    .organisation(NEW_ORGANISATION)
                    .build())
                .build())
        ));
        assertThat(actualResponse).isEqualTo(ASSIGNMENT_RESPONSE);
    }

    @Test
    void shouldTransferLegalCounselWhenSolicitorChanged() {
        List<Element<LegalCounsellor>> legalCounsellors = wrapElements(
            LegalCounsellor.builder().firstName("original").build()
        );
        List<Element<LegalCounsellor>> differentLegalCounsellors = wrapElements(
            LegalCounsellor.builder().firstName("shared").build()
        );

        RespondentSolicitor sharedRepresentative = RespondentSolicitor.builder()
            .firstName(SOLICITOR_FIRST_NAME)
            .lastName(SOLICITOR_LAST_NAME)
            .email(SOLICITOR_USER_EMAIL)
            .organisation(NEW_ORGANISATION)
            .build();
        RespondentSolicitor representativeToRemove = RespondentSolicitor.builder()
            .firstName(OLD_SOLICITOR_FIRST_NAME)
            .lastName(OLD_SOLICITOR_LAST_NAME)
            .email(OLD_SOLICITOR_EMAIL)
            .organisation(OLD_ORGANISATION)
            .build();

        ChangeOrganisationRequest changeRequest = ChangeOrganisationRequest.builder()
            .organisationToAdd(NEW_ORGANISATION)
            .caseRoleId(caseRoleDynamicList("[CHILDSOLICITORA]"))
            .build();

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .children1(wrapElements(
                Child.builder()
                    .party(ChildParty.builder().build())
                    .solicitor(representativeToRemove)
                    .legalCounsellors(legalCounsellors)
                    .build()
            ))
            .respondents1(wrapElements(
                Respondent.builder()
                    .solicitor(sharedRepresentative)
                    .legalCounsellors(differentLegalCounsellors)
                    .build()
            ))
            .changeOrganisationRequestField(changeRequest)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);
        CaseData updatedCaseData = extractCaseData(requestCaptor.getValue().getCaseDetails());

        assertThat(response).isEqualTo(ASSIGNMENT_RESPONSE);
        assertThat(updatedCaseData.getChildren1().get(0).getValue().getLegalCounsellors())
            .isEqualTo(differentLegalCounsellors);
    }

    private Element<Respondent> update(Element<Respondent> respondent, UserDetails solicitor, Organisation org) {
        return element(respondent.getId(), respondent.getValue().toBuilder()
            .legalRepresentation("Yes")
            .solicitor(RespondentSolicitor.builder()
                .firstName(solicitor.getForename())
                .lastName(solicitor.getSurname().orElse(null))
                .email(solicitor.getEmail())
                .organisation(org)
                .build())
            .legalCounsellors(List.of())
            .build());
    }

}
