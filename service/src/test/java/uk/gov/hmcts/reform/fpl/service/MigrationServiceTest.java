package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.NewRespondent;
import uk.gov.hmcts.reform.fpl.model.Respondent;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationServiceTest {

    private final MigrationService service = new MigrationService();

    @Test
    void mapsOldRespondentToNewRespondent() {
        assertThat(service.migrateRespondent(Respondent.builder().build())).isExactlyInstanceOf(NewRespondent.class);
    }
}
