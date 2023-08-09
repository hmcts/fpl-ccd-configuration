package uk.gov.hmcts.reform.fpl.controllers.la;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthoritiesEventData;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLocalAuthoritiesControllerAboutToStartTest extends AbstractCallbackTest {

    @MockBean
    private UserService userService;

    ManageLocalAuthoritiesControllerAboutToStartTest() {
        super("manage-local-authorities");
    }

    @Test
    void shouldAddListOfLocalAuthorities() {

        final CaseData caseData = CaseData.builder()
            .id(TEST_CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_2_CODE)
            .build();

        given(userService.getCaseRoles(TEST_CASE_ID)).willReturn(Set.of(LASOLICITOR));

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        final DynamicList expectedDynamicList = dynamicLists.from(
            Pair.of(LOCAL_AUTHORITY_1_NAME, LOCAL_AUTHORITY_1_CODE),
            Pair.of(LOCAL_AUTHORITY_3_NAME, LOCAL_AUTHORITY_3_CODE));

        final LocalAuthoritiesEventData expectedEventData = LocalAuthoritiesEventData.builder()
            .isLaSolicitor(YesNo.YES)
            .localAuthoritiesToShare(expectedDynamicList)
            .build();

        assertThat(updatedCaseData.getLocalAuthoritiesEventData()).isEqualTo(expectedEventData);
    }

    @Test
    void shouldAddListOfLocalAuthoritiesAndNameOfSharedLocalAuthorityIfExists() {

        final OrganisationPolicy sharedPolicy = organisationPolicy("ORG1", "Organisation 1", LASHARED);

        final CaseData caseData = CaseData.builder()
            .id(TEST_CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_2_CODE)
            .sharedLocalAuthorityPolicy(sharedPolicy)
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        final DynamicList expectedDynamicList = dynamicLists.from(
            Pair.of(LOCAL_AUTHORITY_1_NAME, LOCAL_AUTHORITY_1_CODE),
            Pair.of(LOCAL_AUTHORITY_3_NAME, LOCAL_AUTHORITY_3_CODE));

        final LocalAuthoritiesEventData expectedEventData = LocalAuthoritiesEventData.builder()
            .localAuthoritiesToShare(expectedDynamicList)
            .sharedLocalAuthority(sharedPolicy.getOrganisation().getOrganisationName())
            .build();

        assertThat(updatedCaseData.getLocalAuthoritiesEventData()).isEqualTo(expectedEventData);
    }
}
