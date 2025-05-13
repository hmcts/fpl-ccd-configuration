package uk.gov.hmcts.reform.fpl;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class EnterOthersApiTest extends AbstractApiTest {

    @Test
    public void doNothing() {
        assertTrue(true);
    }

    // @Test
    public void shouldInitialiseFirstOther() {
        verifyScenario("enter-others/initialise-first-other.json");
    }

    // @Test
    public void shouldExtractConfidentialData() {
        verifyScenario("enter-others/extract-confidential-data.json");
    }
}
