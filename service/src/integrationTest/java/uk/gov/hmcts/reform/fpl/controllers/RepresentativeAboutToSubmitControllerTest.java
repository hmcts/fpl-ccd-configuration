package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.OrganisationUser;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
class RepresentativeAboutToSubmitControllerTest extends AbstractCallbackTest {

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private CaseUserApi caseUserApi;

    RepresentativeAboutToSubmitControllerTest() {
        super("manage-representatives");
    }

    @Test
    void shouldAddUsersToCaseAndAssociateRepresentativesWithPerson() {
        final UUID representativeId = UUID.randomUUID();
        final String userId = RandomStringUtils.randomAlphanumeric(10);

        Respondent respondent = Respondent.builder().build();
        Representative representative = Representative.builder()
            .fullName("John Smith")
            .positionInACase("Position")
            .role(RepresentativeRole.REPRESENTING_PERSON_1)
            .servingPreferences(DIGITAL_SERVICE)
            .email("test@test.com")
            .role(RepresentativeRole.REPRESENTING_RESPONDENT_1)
            .build();

        CaseDetails originalCaseDetails = buildCaseData(respondent, emptyList());
        CaseDetails caseDetails = buildCaseData(respondent, List.of(element(representativeId, representative)));

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(originalCaseDetails)
            .caseDetails(caseDetails)
            .build();

        givenFplService();
        given(organisationApi.findUserByEmail(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, representative.getEmail()))
            .willReturn(OrganisationUser.builder().userIdentifier(userId).build());

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);

        verify(organisationApi).findUserByEmail(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, representative.getEmail());

        verify(caseUserApi).updateCaseRolesForUser(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN,
            caseDetails.getId().toString(), userId, new CaseUser(userId, Set.of(SOLICITOR.formattedName())));

        CaseData outgoingCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        Respondent updatedResponded = outgoingCaseData.getRespondents1().get(0).getValue();

        assertThat(unwrapElements(updatedResponded.getRepresentedBy())).containsExactly(representativeId);
        assertThat(callbackResponse.getErrors()).isNullOrEmpty();
    }

    private static CaseDetails buildCaseData(Respondent respondent, List<Element<Representative>> representatives) {
        return CaseDetails.builder()
            .id(RandomUtils.nextLong())
            .data(Map.of(
                "representatives", representatives,
                "respondents1", wrapElements(respondent)))
            .build();
    }

}
