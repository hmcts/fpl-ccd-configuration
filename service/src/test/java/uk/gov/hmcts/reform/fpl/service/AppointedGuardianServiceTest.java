package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

class AppointedGuardianServiceTest {

    private final AppointedGuardianService underTest = new AppointedGuardianService();

    @Test
    void shouldDoSomething() {
        CaseData caseData = CaseData.builder().build();
        String formattedNames = underTest.getAppointedGuardiansNames(caseData);
        assertThat(formattedNames).isEqualTo("Something here");
    }

}
