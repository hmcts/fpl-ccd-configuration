package uk.gov.hmcts.reform.fpl;

import org.junit.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;

public class CreateCaseApiTest extends AbstractApiTest {

    @Test
    public void doNothing() {
        assertTrue(true);
    }

    // @Test
    public void shouldCreateAndShareCase() {

        CaseData caseData = caseService.createCase(LA_WILTSHIRE_USER_1);

        assertThat(caseData.getId()).isNotNull();

        assertThat(caseData.getState()).isEqualTo(OPEN);

        assertThat(caseData.getCaseLocalAuthority()).isEqualTo("SNW");

        assertThat(caseData.getLocalAuthorityPolicy().getOrganisation()).isEqualTo(Organisation.builder()
            .organisationID(configValue("WiltshireOrganisationID").toString())
            .organisationName("Wiltshire County Council")
            .build());

        assertThat(caseService.hasCaseAccess(LA_WILTSHIRE_USER_2, caseData)).isTrue();
        assertThat(caseService.hasCaseAccess(LA_SWANSEA_USER_1, caseData)).isFalse();
    }
}
