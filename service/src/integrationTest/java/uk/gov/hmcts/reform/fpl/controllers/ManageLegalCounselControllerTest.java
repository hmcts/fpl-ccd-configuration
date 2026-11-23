package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageLegalCounselEventData;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.rd.model.Organisation;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.LegalCounsellorTestHelper.buildLegalCounsellor;
import static uk.gov.hmcts.reform.fpl.utils.RespondentsTestHelper.respondents;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLegalCounselControllerTest extends AbstractCallbackTest {


    @MockBean
    private OrganisationService organisationService;
    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    private CaseData caseData;
    private LegalCounsellor legalCounsellor;

    ManageLegalCounselControllerTest() {
        super("manage-legal-counsel");
    }

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder().id(TEST_CASE_ID).respondents1(respondents()).build();
        legalCounsellor = buildLegalCounsellor("1");

        givenFplService();

        givenCaseRoles(TEST_CASE_ID, USER_ID, CaseRole.SOLICITORA);

        when(organisationService.findUserByEmail(legalCounsellor.getEmail())).thenReturn(Optional.of(USER_ID));
        when(coreCaseDataApi.getCase(
            any(),
            any(),
            eq(TEST_CASE_ID.toString())
        )).thenAnswer(invocation -> CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(toMap(caseData))
            .build());
    }

    @Test
    void shouldReturnNoLegalCounselForSolicitorUserWithNoLegalCounsel() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getWarnings()).isNullOrEmpty();
        assertThat(response.getErrors()).isNullOrEmpty();
        CaseData returnedCaseData = extractCaseData(response);
        assertThat(returnedCaseData.getManageLegalCounselEventData().getLegalCounsellors()).isEmpty();
    }

    @Test
    void shouldReturnExistingLegalCounselForSolicitorUserWithExistingLegalCounsel() {
        List<Element<LegalCounsellor>> legalCounsellors = List.of(element(legalCounsellor));
        caseData.getAllRespondents().get(0).getValue().setLegalCounsellors(legalCounsellors);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getWarnings()).isNullOrEmpty();
        assertThat(response.getErrors()).isNullOrEmpty();
        CaseData returnedCaseData = extractCaseData(response);
        assertThat(returnedCaseData.getManageLegalCounselEventData().getLegalCounsellors()).isEqualTo(legalCounsellors);
    }

    @Test
    void shouldReturnNoErrorMessageWhenMidEventValidationPasses() {
        caseData = caseData.toBuilder()
            .manageLegalCounselEventData(
                ManageLegalCounselEventData.builder()
                    .legalCounsellors(wrapElements(legalCounsellor))
                    .build()
            ).build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData);

        assertThat(response.getWarnings()).isNullOrEmpty();
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getData()).isEqualTo(toMap(caseData));
    }

    @Test
    void shouldReturnErrorMessageWhenMidEventValidationFails() {
        LegalCounsellor legalCounsellorWithNoOrganisation = legalCounsellor.toBuilder().organisation(null).build();
        caseData = caseData.toBuilder()
            .manageLegalCounselEventData(
                ManageLegalCounselEventData.builder()
                    .legalCounsellors(wrapElements(legalCounsellorWithNoOrganisation))
                    .build()
            ).build();

        when(organisationService.findUserByEmail(legalCounsellorWithNoOrganisation.getEmail()))
            .thenReturn(Optional.of(USER_ID));

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData);

        assertThat(response.getWarnings()).isNullOrEmpty();
        assertThat(response.getData()).isEqualTo(toMap(caseData));
        assertThat(response.getErrors())
            .hasSize(1)
            .contains(format("Legal counsellor %s has no selected organisation",
                legalCounsellorWithNoOrganisation.getFullName()));
    }

    @Test
    void shouldRemoveLegalCounsellorsFromEventBeforeSubmitting() {
        UUID elementId = UUID.randomUUID();
        List<Element<LegalCounsellor>> legalCounsellors = List.of(element(elementId, legalCounsellor));
        caseData = caseData.toBuilder()
            .manageLegalCounselEventData(
                ManageLegalCounselEventData.builder().legalCounsellors(legalCounsellors).build()
            ).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        List<Element<LegalCounsellor>> updatedLegalCounsellors = List.of(element(
            elementId, legalCounsellor.toBuilder().userId(USER_ID).build()
        ));

        assertThat(response.getWarnings()).isNullOrEmpty();
        assertThat(response.getErrors()).isNullOrEmpty();
        CaseData returnedCaseData = extractCaseData(response);
        assertThat(returnedCaseData.getManageLegalCounselEventData().getLegalCounsellors()).isNull();
        assertThat(returnedCaseData.getAllRespondents().get(0).getValue().getLegalCounsellors())
            .isEqualTo(updatedLegalCounsellors);
    }

    @Test
    void shouldReturnErrorWhenThirdPartyApplicantSolicitorAttemptsToManageRespondentCounsel() {
        String applicantOrgId = "third-party-applicant-org";
        String respondentOrgId = "respondent-solicitor-org";

        Respondent respondent = caseData.getAllRespondents().getFirst().getValue();

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(respondentOrgId)
                .build())
            .build();

        List<Element<Respondent>> respondents = List.of(
            element(
                caseData.getAllRespondents().getFirst().getId(),
                respondent.toBuilder()
                    .solicitor(respondentSolicitor)
                    .build()
            )
        );

        caseData = caseData.toBuilder()
            .representativeType(RepresentativeType.RESPONDENT_SOLICITOR)
            .respondents1(respondents)
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                    .organisationID(applicantOrgId)
                    .build())
                .build())
            .build();

        when(organisationService.findOrganisation())
            .thenReturn(Optional.of(Organisation.builder()
                .organisationIdentifier(applicantOrgId)
                .build()));

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getErrors())
            .containsExactly(
                "You are not able to manage legal counsel for this respondent using this feature. "
                    + "Please use the ‘Notice of Change’ section to manage representation for the respondent."
            );

        assertThat(response.getData().get("legalCounsellors")).isNull();
    }


    @Test
    void shouldReturnErrorWhenThirdPartyApplicantSolicitorAttemptsToSubmitRespondentCounsel() {
        String applicantOrgId = "third-party-applicant-org";
        String respondentOrgId = "respondent-solicitor-org";

        Respondent respondent = caseData.getAllRespondents().getFirst().getValue();

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(respondentOrgId)
                .build())
            .build();

        List<Element<Respondent>> respondents = List.of(
            element(
                caseData.getAllRespondents().getFirst().getId(),
                respondent.toBuilder()
                    .solicitor(respondentSolicitor)
                    .build()
            )
        );

        caseData = caseData.toBuilder()
            .representativeType(RepresentativeType.RESPONDENT_SOLICITOR)
            .respondents1(respondents)
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                    .organisationID(applicantOrgId)
                    .build())
                .build())
            .manageLegalCounselEventData(
                ManageLegalCounselEventData.builder()
                    .legalCounsellors(wrapElements(legalCounsellor))
                    .build())
            .build();

        when(organisationService.findOrganisation())
            .thenReturn(Optional.of(Organisation.builder()
                .organisationIdentifier(applicantOrgId)
                .build()));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        assertThat(response.getErrors())
            .containsExactly(
                "You are not able to manage legal counsel for this respondent using this feature. "
                    + "Please use the ‘Notice of Change’ section to manage representation for the respondent."
            );

        assertThat(response.getData()).isEqualTo(toMap(caseData));
    }


    @Test
    void shouldAllowRespondentSolicitorToManageCounselInThirdPartyApplication() {
        String applicantOrgId = "third-party-applicant-org";
        String respondentOrgId = "respondent-solicitor-org";

        Respondent respondent = caseData.getAllRespondents().getFirst().getValue();

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(respondentOrgId)
                .build())
            .build();

        List<Element<Respondent>> respondents = List.of(
            element(
                caseData.getAllRespondents().getFirst().getId(),
                respondent.toBuilder()
                    .solicitor(respondentSolicitor)
                    .build()
            )
        );

        caseData = caseData.toBuilder()
            .representativeType(RepresentativeType.RESPONDENT_SOLICITOR)
            .respondents1(respondents)
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                    .organisationID(applicantOrgId)
                    .build())
                .build())
            .build();

        when(organisationService.findOrganisation())
            .thenReturn(Optional.of(Organisation.builder()
                .organisationIdentifier(respondentOrgId)
                .build()));

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getWarnings()).isNullOrEmpty();

        CaseData returnedCaseData = extractCaseData(response);

        assertThat(returnedCaseData.getManageLegalCounselEventData()
            .getLegalCounsellors()).isEmpty();
    }


    @Test
    void shouldAllowRespondentSolicitorToSubmitCounselInThirdPartyApplication() {
        String applicantOrgId = "third-party-applicant-org";
        String respondentOrgId = "respondent-solicitor-org";

        UUID elementId = UUID.randomUUID();

        Respondent respondent = caseData.getAllRespondents().getFirst().getValue();

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(respondentOrgId)
                .build())
            .build();

        List<Element<Respondent>> respondents = List.of(
            element(
                caseData.getAllRespondents().getFirst().getId(),
                respondent.toBuilder()
                    .solicitor(respondentSolicitor)
                    .build()
            )
        );

        caseData = caseData.toBuilder()
            .representativeType(RepresentativeType.RESPONDENT_SOLICITOR)
            .respondents1(respondents)
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                    .organisationID(applicantOrgId)
                    .build())
                .build())
            .manageLegalCounselEventData(
                ManageLegalCounselEventData.builder()
                    .legalCounsellors(List.of(element(elementId, legalCounsellor)))
                    .build())
            .build();

        when(organisationService.findOrganisation())
            .thenReturn(Optional.of(Organisation.builder()
                .organisationIdentifier(respondentOrgId)
                .build()));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getWarnings()).isNullOrEmpty();

        CaseData returnedCaseData = extractCaseData(response);

        assertThat(returnedCaseData.getManageLegalCounselEventData()
            .getLegalCounsellors()).isNull();

        assertThat(returnedCaseData.getAllRespondents().get(0)
            .getValue().getLegalCounsellors())
            .containsExactly(
                element(elementId, legalCounsellor.toBuilder()
                    .userId(USER_ID)
                    .build())
            );
    }

}
