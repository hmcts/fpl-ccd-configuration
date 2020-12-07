package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
public class MigrateCaseControllerTest extends AbstractControllerTest {

    private String migrationId;

    protected MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @BeforeEach
    void setCaseIdentifiers() {
        migrationId = "FPLA-2379";

    }

    @Test
    void shouldNotReturnErrorsWithCorrectMigrationId() {
        CaseDetails caseDetails = callbackRequest().getCaseDetails();
        caseDetails.getData().put("migrationId", migrationId);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).isNull();
    }

    @Test
    void shouldNotRunMigrationWhenMigrationIdHasBeenDifferent() {
        migrationId = "some random number";
        CaseDetails caseDetails = callbackRequest().getCaseDetails();
        caseDetails.getData().put("migrationId", migrationId);

        postAboutToSubmitEvent(caseDetails);

        assertThat(caseDetails.getData().containsKey(migrationId));
    }
}
