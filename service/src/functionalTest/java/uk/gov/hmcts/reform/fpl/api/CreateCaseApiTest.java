package uk.gov.hmcts.reform.fpl.api;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseService;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.api.ApiTestService.LA_SWANSEA_USER_1;
import static uk.gov.hmcts.reform.fpl.api.ApiTestService.LA_WILTSHIRE_USER_1;
import static uk.gov.hmcts.reform.fpl.api.ApiTestService.LA_WILTSHIRE_USER_2;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateCaseApiTest extends AbstractApiTest {

    final CaseService caseService;
    final ApiTestService apiTestService;

    @Test
    public void shouldCreateAndShareCase() {

        CaseData caseData = caseService.createCase(LA_WILTSHIRE_USER_1);

        assertThat(caseData.getId()).isNotNull();

        assertThat(caseData.getState()).isEqualTo(OPEN);

        assertThat(caseData.getCaseLocalAuthority()).isEqualTo("SNW");

        assertThat(caseData.getLocalAuthorityPolicy().getOrganisation()).isEqualTo(Organisation.builder()
            .organisationID(apiTestService.configValue("WiltshireOrganisationID").toString())
            .organisationName("Wiltshire County Council")
            .build());

        assertThat(caseService.hasCaseAccess(LA_WILTSHIRE_USER_2, caseData)).isTrue();
        assertThat(caseService.hasCaseAccess(LA_SWANSEA_USER_1, caseData)).isFalse();
    }
}
