package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageLegalCounselEventData;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;

import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.SOLICITORA;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.LegalCounsellorTestHelper.buildLegalCounsellorAndMockUserId;
import static uk.gov.hmcts.reform.fpl.utils.LegalCounsellorTestHelper.buildLegalCounsellorWithOrganisationAndMockUserId;
import static uk.gov.hmcts.reform.fpl.utils.RespondentsTestHelper.respondents;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLegalCounselControllerTest extends AbstractCallbackTest {

    @MockBean
    private OrganisationService organisationService;

    private CaseData caseData;
    private LegalCounsellor legalCounsellor;

    ManageLegalCounselControllerTest() {
        super("manage-legal-counsel");
    }

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .id(TEST_CASE_ID)
            .respondents1(respondents())
            .build();

        legalCounsellor = buildLegalCounsellorWithOrganisationAndMockUserId(organisationService, "1").getValue();
        givenFplService();
        givenCaseRoles(TEST_CASE_ID, USER_ID, SOLICITORA);
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
                ManageLegalCounselEventData.builder().legalCounsellors(singletonList(element(legalCounsellor))).build()
            ).build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData);

        assertThat(response.getWarnings()).isNullOrEmpty();
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getData()).isEqualTo(toMap(caseData));
    }

    @Test
    void shouldReturnErrorMessageWhenMidEventValidationFails() {
        LegalCounsellor legalCounsellorWithNoOrganisation =
            buildLegalCounsellorAndMockUserId(organisationService, "2").getValue();
        caseData = caseData.toBuilder()
            .manageLegalCounselEventData(
                ManageLegalCounselEventData.builder().legalCounsellors(singletonList(
                    element(legalCounsellorWithNoOrganisation)
                )).build()
            ).build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData);

        assertThat(response.getWarnings()).isNullOrEmpty();
        assertThat(response.getData()).isEqualTo(toMap(caseData));
        assertThat(response.getErrors())
            .containsExactly(format("Legal counsellor %s has no selected organisation",
                legalCounsellorWithNoOrganisation.getFullName()));
    }

    @Test
    void shouldRemoveLegalCounsellorsFromEventBeforeSubmitting() {
        List<Element<LegalCounsellor>> legalCounsellors = singletonList(element(legalCounsellor));
        caseData = caseData.toBuilder()
            .manageLegalCounselEventData(
                ManageLegalCounselEventData.builder().legalCounsellors(legalCounsellors).build()
            ).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        assertThat(response.getWarnings()).isNullOrEmpty();
        assertThat(response.getErrors()).isNullOrEmpty();
        CaseData returnedCaseData = extractCaseData(response);
        assertThat(returnedCaseData.getManageLegalCounselEventData().getLegalCounsellors()).isNull();
        assertThat(returnedCaseData.getAllRespondents().get(0).getValue().getLegalCounsellors())
            .isEqualTo(legalCounsellors);
    }

}
