package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

class MigrationServiceTest {

    private final MigrationService service = new MigrationService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    @Test
    void shouldMapCaseDetailsToNewCaseDetails() throws IOException {
        CaseDetails caseDetails = service.migrateCase(populatedCaseDetails());

        List<Map<String, Object>> respondents = objectMapper.convertValue(caseDetails.getData().get("respondents"), List.class);

        assertThat(respondents).hasSize(3);
    }
}
