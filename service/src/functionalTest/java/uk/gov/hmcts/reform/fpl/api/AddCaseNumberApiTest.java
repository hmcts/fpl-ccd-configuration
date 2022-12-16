package uk.gov.hmcts.reform.fpl.api;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AddCaseNumberApiTest extends AbstractApiTest {

    final ApiTestService apiTestService;

    @Test
    public void shouldSendCaseDetailsToRobotics() {
        apiTestService.verifyScenario("case-number/add-case-number-on-submission.json");
    }
}
