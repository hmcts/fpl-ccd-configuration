package uk.gov.hmcts.reform.fpl.api;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EnterOthersApiTest extends AbstractApiTest {

    final ApiTestService apiTestService;

    @Test
    public void shouldInitialiseFirstOther() {
        apiTestService.verifyScenario("enter-others/initialise-first-other.json");
    }

    @Test
    public void shouldExtractConfidentialData() {
        apiTestService.verifyScenario("enter-others/extract-confidential-data.json");
    }
}
