package uk.gov.hmcts.reform.fpl.controllers.la;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthoritiesEventData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_NAME;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLocalAuthoritiesControllerAboutToStartTest extends AbstractCallbackTest {

    ManageLocalAuthoritiesControllerAboutToStartTest() {
        super("manage-local-authorities");
    }

    @Test
    void shouldAddListOfLocalAuthorities() {

        final CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_2_CODE)
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        final DynamicList expectedDynamicList = dynamicLists.from(
            Pair.of(LOCAL_AUTHORITY_1_NAME, LOCAL_AUTHORITY_1_CODE),
            Pair.of(LOCAL_AUTHORITY_3_NAME, LOCAL_AUTHORITY_3_CODE));

        final LocalAuthoritiesEventData expectedEventData = LocalAuthoritiesEventData.builder()
            .localAuthoritiesToShare(expectedDynamicList)
            .build();

        assertThat(updatedCaseData.getLocalAuthoritiesEventData()).isEqualTo(expectedEventData);
    }
}
