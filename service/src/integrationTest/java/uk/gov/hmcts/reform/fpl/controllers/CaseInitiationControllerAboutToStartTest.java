package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.testingsupport.DynamicListHelper;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_PRIVATE_ORG_ID;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOrganisation;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationControllerAboutToStartTest extends AbstractControllerTest {

    @Autowired
    private DynamicListHelper dynamicLists;

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    CaseInitiationControllerAboutToStartTest() {
        super("case-initiation");
    }

    @BeforeEach
    void setup() {
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldReturnListOfOutsourcingLAsIfUserIsAllowedToCreateCaseOnBehalfOfLAs() {
        final Organisation organisation = testOrganisation(DEFAULT_PRIVATE_ORG_ID);

        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(organisation);

        Map<String, Object> caseData = postAboutToStartEvent(CaseData.builder().build()).getData();

        assertThat(caseData).extracting("outsourcingLAs")
            .isEqualTo(dynamicLists.asMap(Pair.of(DEFAULT_LA_NAME, DEFAULT_LA_CODE)));
    }

    @Test
    void shouldNotReturnListOfOutsourcingLAsIfUserIsNotAllowedToCreateCaseOnBehalfOfLAs() {
        final Organisation organisation = testOrganisation("PROHIBITED");

        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(organisation);

        Map<String, Object> caseData = postAboutToStartEvent(CaseData.builder().build()).getData();

        assertThat(caseData).extracting("outsourcingLAs").isNull();
    }

}
