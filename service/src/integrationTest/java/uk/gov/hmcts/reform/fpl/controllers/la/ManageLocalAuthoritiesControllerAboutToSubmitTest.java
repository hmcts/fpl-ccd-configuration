package uk.gov.hmcts.reform.fpl.controllers.la;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.aac.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.aac.model.DecisionRequest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthoritiesEventData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.ContactInformation;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_ID;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_ID;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_NAME;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.ADD;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.REMOVE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLocalAuthoritiesControllerAboutToSubmitTest extends AbstractCallbackTest {

    @Autowired
    private Time time;

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @Captor
    private ArgumentCaptor<DecisionRequest> decisionCaptor;

    ManageLocalAuthoritiesControllerAboutToSubmitTest() {
        super("manage-local-authorities");
    }

    private final Organisation organisation = Organisation.builder()
        .organisationIdentifier(LOCAL_AUTHORITY_2_ID)
        .name(LOCAL_AUTHORITY_2_NAME)
        .contactInformation(List.of(ContactInformation.builder()
            .addressLine1("Line 1")
            .postCode("AB 100")
            .build()))
        .build();

    private final AboutToStartOrSubmitCallbackResponse nocResponse = AboutToStartOrSubmitCallbackResponse.builder()
        .data(Map.of("key", "value"))
        .build();

    @BeforeEach
    void setup() {
        givenFplService();
        givenSystemUser();

        given(organisationApi.findOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, LOCAL_AUTHORITY_2_ID))
            .willReturn(organisation);

        given(caseAssignmentApi.applyDecision(eq(USER_AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), decisionCaptor.capture()))
            .willReturn(nocResponse);
    }

    @Test
    void shouldAddSecondaryLocalAuthority() {

        final DynamicList localAuthorities = dynamicLists.from(0,
            Pair.of(LOCAL_AUTHORITY_2_NAME, LOCAL_AUTHORITY_2_CODE),
            Pair.of(LOCAL_AUTHORITY_3_NAME, LOCAL_AUTHORITY_3_CODE));

        final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
            .localAuthorityAction(ADD)
            .localAuthoritiesToShare(localAuthorities)
            .localAuthorityEmail("test@test.com")
            .build();

        final LocalAuthority designatedLocalAuthority = LocalAuthority.builder()
            .id(LOCAL_AUTHORITY_1_ID)
            .name(LOCAL_AUTHORITY_1_NAME)
            .designated("Yes")
            .build();

        final CaseData caseData = CaseData.builder()
            .sharedLocalAuthorityPolicy(null)
            .localAuthorityPolicy(organisationPolicy(LOCAL_AUTHORITY_1_ID, LOCAL_AUTHORITY_1_NAME, LASOLICITOR))
            .localAuthoritiesEventData(eventData)
            .localAuthorities(wrapElements(designatedLocalAuthority))
            .build();

        final LocalAuthority expectedSecondaryLocalAuthority = LocalAuthority.builder()
            .id(LOCAL_AUTHORITY_2_ID)
            .name(LOCAL_AUTHORITY_2_NAME)
            .email("test@test.com")
            .designated("No")
            .address(Address.builder()
                .addressLine1("Line 1")
                .postcode("AB 100")
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        final OrganisationPolicy expectedSharedOrganisationPolicy = organisationPolicy(LOCAL_AUTHORITY_2_ID,
            LOCAL_AUTHORITY_2_NAME, LASHARED);

        assertThat(updatedCaseData.getLocalAuthorities()).extracting(Element::getValue)
            .containsExactly(designatedLocalAuthority, expectedSecondaryLocalAuthority);

        assertThat(updatedCaseData.getSharedLocalAuthorityPolicy()).isEqualTo(expectedSharedOrganisationPolicy);

        verifyNoInteractions(caseAssignmentApi);
    }

    @Test
    void shouldRemoveSecondaryLocalAuthority() {

        final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
            .localAuthorityAction(REMOVE)
            .localAuthorityToRemove(LOCAL_AUTHORITY_2_NAME)
            .build();

        final LocalAuthority designatedLocalAuthority = LocalAuthority.builder()
            .id(LOCAL_AUTHORITY_1_ID)
            .name(LOCAL_AUTHORITY_1_NAME)
            .designated("Yes")
            .build();

        final LocalAuthority secondaryLocalAuthority = LocalAuthority.builder()
            .id(LOCAL_AUTHORITY_2_ID)
            .name(LOCAL_AUTHORITY_2_NAME)
            .build();

        final CaseData caseData = CaseData.builder()
            .sharedLocalAuthorityPolicy(organisationPolicy(LOCAL_AUTHORITY_2_ID, LOCAL_AUTHORITY_2_NAME, LASHARED))
            .localAuthorityPolicy(organisationPolicy(LOCAL_AUTHORITY_1_ID, LOCAL_AUTHORITY_1_NAME, LASOLICITOR))
            .localAuthoritiesEventData(eventData)
            .localAuthorities(wrapElements(designatedLocalAuthority, secondaryLocalAuthority))
            .build();

        final ChangeOrganisationRequest expectedChangeRequest = ChangeOrganisationRequest.builder()
            .caseRoleId(caseRoleDynamicList(LASHARED.formattedName()))
            .requestTimestamp(time.now())
            .approvalStatus(APPROVED)
            .organisationToRemove(caseData.getSharedLocalAuthorityPolicy().getOrganisation())
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        final CaseData updatedCaseData = extractCaseData(decisionCaptor.getValue().getCaseDetails());

        assertThat(response).isEqualTo(nocResponse);

        assertThat(updatedCaseData.getLocalAuthorities()).extracting(Element::getValue)
            .containsExactly(designatedLocalAuthority);

        assertThat(updatedCaseData.getChangeOrganisationRequestField()).isEqualTo(expectedChangeRequest);

        verifyNoInteractions(organisationApi);
    }

}
