package uk.gov.hmcts.reform.fpl.controllers.la;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthoritiesEventData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_NAME;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.ADD;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.REMOVE;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLocalAuthoritiesControllerMidEventsTest extends AbstractCallbackTest {

    ManageLocalAuthoritiesControllerMidEventsTest() {
        super("manage-local-authorities");
    }

    @Test
    void shouldReturnErrorWhenUserTriesToRemoveNonExistingSecondaryLocalAuthority() {

        final CaseData caseData = CaseData.builder()
            .sharedLocalAuthorityPolicy(null)
            .localAuthoritiesEventData(LocalAuthoritiesEventData.builder()
                .localAuthorityAction(REMOVE)
                .build())
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "local-authority");

        assertThat(response.getErrors())
            .containsExactly("There are no other local authorities to remove from this case");
    }

    @Test
    void shouldReturnErrorWhenUserTriesToAddAnotherSharedLocalAuthority() {

        final CaseData caseData = CaseData.builder()
            .sharedLocalAuthorityPolicy(organisationPolicy("ORG", "ORG name", LASHARED))
            .localAuthoritiesEventData(LocalAuthoritiesEventData.builder()
                .localAuthorityAction(ADD)
                .build())
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "local-authority");

        assertThat(response.getErrors())
            .containsExactly("Case access has already been given to local authority. Remove their access to continue");
    }

    @Test
    void shouldAddNameOfLocalAuthorityToBeRemoved() {

        final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
            .localAuthorityAction(REMOVE)
            .build();

        final CaseData caseData = CaseData.builder()
            .sharedLocalAuthorityPolicy(organisationPolicy("ORG", "ORG name", LASHARED))
            .localAuthoritiesEventData(eventData)
            .build();

        final CaseData updated = extractCaseData(postMidEvent(caseData, "local-authority"));

        final LocalAuthoritiesEventData expectedEventData = LocalAuthoritiesEventData.builder()
            .localAuthorityAction(REMOVE)
            .localAuthorityToRemove("ORG name")
            .build();

        assertThat(updated.getLocalAuthoritiesEventData()).isEqualTo(expectedEventData);
    }

    @Test
    void shouldAddEmailOfLocalAuthorityToBeAdded() {

        final DynamicList localAuthorities = dynamicLists.from(1,
            Pair.of(LOCAL_AUTHORITY_1_NAME, LOCAL_AUTHORITY_1_CODE),
            Pair.of(LOCAL_AUTHORITY_2_NAME, LOCAL_AUTHORITY_2_CODE),
            Pair.of(LOCAL_AUTHORITY_3_NAME, LOCAL_AUTHORITY_3_CODE));

        final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
            .localAuthorityAction(ADD)
            .localAuthoritiesToShare(localAuthorities)
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthoritiesEventData(eventData)
            .build();

        final CaseData updated = extractCaseData(postMidEvent(caseData, "local-authority"));

        final LocalAuthoritiesEventData expectedEventData = LocalAuthoritiesEventData.builder()
            .localAuthorityAction(ADD)
            .localAuthoritiesToShare(localAuthorities)
            .localAuthorityEmail(LOCAL_AUTHORITY_2_INBOX)
            .build();

        assertThat(updated.getLocalAuthoritiesEventData()).isEqualTo(expectedEventData);
    }

    @Test
    void shouldReturnValidateErrorsWhenProvidedEmailIsNotValid() {

        final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
            .localAuthorityAction(ADD)
            .localAuthorityEmail("test")
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthoritiesEventData(eventData)
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "local-authority-validate");

        assertThat(response.getErrors())
            .containsExactly("Enter an email address in the correct format, for example name@example.com");

    }

    @Test
    void shouldNotReturnErrorsWhenProvidedEmailIsValid() {

        final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
            .localAuthorityAction(ADD)
            .localAuthorityEmail("test@test.com")
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthoritiesEventData(eventData)
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "local-authority-validate");

        assertThat(response.getErrors()).isEmpty();
    }
}
