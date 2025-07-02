package uk.gov.hmcts.reform.fpl;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AddCaseNumberApiTest extends AbstractApiTest {

    @Test
    public void doNothing() {
        assertTrue(true);
    }

    //@Test
    public void shouldSendCaseDetailsToRobotics() {
        verifyScenario("case-number/add-case-number-on-submission.json");
    }
}
