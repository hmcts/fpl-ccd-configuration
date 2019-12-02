package uk.gov.hmcts.reform.fpl.controllers;

import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCMOController.class)
@OverrideAutoConfiguration(enabled = true)
public class ActionCMOControllerTest {
    // TODO: 02/12/2019 Write the Tests once branches all pulled in
}
