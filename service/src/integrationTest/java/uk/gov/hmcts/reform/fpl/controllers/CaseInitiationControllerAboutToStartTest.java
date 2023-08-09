package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.testingsupport.DynamicListHelper;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_ID;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_USER_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.PRIVATE_ORG_ID;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOrganisation;

@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationControllerAboutToStartTest extends AbstractCallbackTest {

    @Autowired
    private DynamicListHelper dynamicLists;

    @MockBean
    private OrganisationApi organisationApi;

    CaseInitiationControllerAboutToStartTest() {
        super("case-initiation");
    }

    @BeforeEach
    void setup() {
        givenFplService();
    }

    @Test
    void shouldReturnListOfOutsourcingLAsIfPrivateSolicitorAllowedToCreateCaseOnBehalfOfLAs() {
        final Organisation organisation = testOrganisation(PRIVATE_ORG_ID);

        givenCurrentUser(UserInfo.builder().sub("test@private.solicitors.uk").build());

        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(organisation);

        Map<String, Object> caseData = postAboutToStartEvent(CaseData.builder().build()).getData();

        assertThat(caseData).extracting("outsourcingLAs")
            .isEqualTo(dynamicLists.asMap(Pair.of(LOCAL_AUTHORITY_1_NAME, LOCAL_AUTHORITY_1_CODE)));

        assertThat(caseData).extracting("outsourcingType").isEqualTo("EPS");
    }

    @Test
    void shouldReturnListOfOutsourcingLAsIfLASolicitorAllowedToCreateCaseOnBehalfOfOtherLA() {
        final Organisation organisation = testOrganisation(LOCAL_AUTHORITY_2_ID);

        givenCurrentUser(UserInfo.builder().sub(LOCAL_AUTHORITY_2_USER_EMAIL).build());

        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(organisation);

        Map<String, Object> caseData = postAboutToStartEvent(CaseData.builder().build()).getData();

        assertThat(caseData).extracting("outsourcingLAs")
            .isEqualTo(dynamicLists.asMap(
                0,
                Pair.of(LOCAL_AUTHORITY_2_NAME, LOCAL_AUTHORITY_2_CODE),
                Pair.of(LOCAL_AUTHORITY_1_NAME, LOCAL_AUTHORITY_1_CODE)
            ));

        assertThat(caseData).extracting("outsourcingType").isEqualTo("MLA");
    }

    @Test
    void shouldNotReturnListOfOutsourcingLAsIfUserIsNotAllowedToCreateCaseOnBehalfOfLAs() {
        final Organisation organisation = testOrganisation("PROHIBITED");

        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(organisation);

        Map<String, Object> caseData = postAboutToStartEvent(CaseData.builder().build()).getData();

        assertThat(caseData).extracting("outsourcingLAs").isNull();
        assertThat(caseData).extracting("outsourcingType").isNull();
    }

}
