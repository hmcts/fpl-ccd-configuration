package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.OutsourcingType;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_USER_EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOrganisation;

@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationControllerMidEventTest extends AbstractCallbackTest {

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    CaseInitiationControllerMidEventTest() {
        super("case-initiation");
    }

    @BeforeEach
    void setup() {
        givenFplService();
        givenCurrentUserWithEmail(LOCAL_AUTHORITY_1_USER_EMAIL);
    }

    @Test
    void shouldNotPopulateErrorsWhenToggleIsEnabled() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .representativeType(RepresentativeType.LOCAL_AUTHORITY)
            .build();

        given(featureToggleService.isCaseCreationForNotOnboardedUsersEnabled(anyString())).willReturn(true);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData);

        assertThat(response.getErrors().isEmpty());
    }

    @Test
    void shouldNotPopulateErrorsWhenToggleIsDisabledAndUserHasBeenOnboarded() {
        Organisation organisation = testOrganisation();
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .representativeType(RepresentativeType.LOCAL_AUTHORITY)
            .build();

        given(featureToggleService.isCaseCreationForNotOnboardedUsersEnabled(anyString())).willReturn(true);
        given(organisationService.findOrganisation()).willReturn(Optional.of(organisation));

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldPopulateErrorsWhenToggleIsDisabledAndUserHasNotBeenOnboarded() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .representativeType(RepresentativeType.LOCAL_AUTHORITY)
            .build();

        given(featureToggleService.isCaseCreationForNotOnboardedUsersEnabled(anyString())).willReturn(false);

        given(organisationService.findOrganisation()).willReturn(Optional.empty());

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData);

        assertThat(response.getErrors()).containsExactly("Register for an account.",
            "You cannot start an online application until you’re fully registered.",
            "Ask your local authority’s public law administrator, or email MyHMCTSsupport@justice.gov.uk, "
                + "for help with registration.");
    }

    @Test
    void shouldGetListOfUsersIfOutsourced() {
        Organisation organisation = testOrganisation();
        given(featureToggleService.isCaseCreationForNotOnboardedUsersEnabled(anyString())).willReturn(true);
        given(organisationService.findOrganisation()).willReturn(Optional.of(organisation));

        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .representativeType(RepresentativeType.LOCAL_AUTHORITY)
            .outsourcingType(OutsourcingType.MLA)
            .outsourcingLAs(DynamicList.builder()
                .value(DynamicListElement.builder().label("SW").code("SW").build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData);

        assertThat(response.getData().get("isOutsourcedCase")).isEqualTo(YesNo.YES.getValue());
    }
}
