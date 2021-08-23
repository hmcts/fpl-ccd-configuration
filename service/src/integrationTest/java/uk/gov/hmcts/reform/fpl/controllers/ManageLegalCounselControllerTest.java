package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRole;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageLegalCounselEventData;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID_AS_LONG;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORA;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.LegalCounsellorTestHelper.buildLegalCounsellor;
import static uk.gov.hmcts.reform.fpl.utils.RespondentsTestHelper.respondents;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLegalCounselControllerTest extends AbstractCallbackTest {

    @MockBean
    private CaseAccessDataStoreApi caseDataAccessApi;

    @MockBean
    private OrganisationService organisationService;

    private CaseData caseData;
    private LegalCounsellor legalCounsellor;

    protected ManageLegalCounselControllerTest() {
        super("manage-legal-counsel");
    }

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder().id(TEST_CASE_ID_AS_LONG).respondents1(respondents()).build();
        legalCounsellor = buildLegalCounsellor("1");

        givenFplService();

        when(caseDataAccessApi.getUserRoles(
            USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, List.of(TEST_CASE_ID), List.of(USER_ID)
        )).thenReturn(
            CaseAssignedUserRolesResource.builder()
                .caseAssignedUserRoles(List.of(
                    CaseAssignedUserRole.builder().caseRole(SOLICITORA.getCaseRoleLabel()).build()
                ))
                .build()
        );

        when(organisationService.findUserByEmail(legalCounsellor.getEmail())).thenReturn(Optional.of(USER_ID));
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

}
