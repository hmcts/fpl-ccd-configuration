package uk.gov.hmcts.reform.fpl;

import org.junit.Test;

public class AddCaseNumberApiTest extends AbstractApiTest {

    @Test
    public void shouldSendCaseDetailsToRobotics() {
        verifyScenario("case-number/add-case-number-on-submission.json");
    }
}
