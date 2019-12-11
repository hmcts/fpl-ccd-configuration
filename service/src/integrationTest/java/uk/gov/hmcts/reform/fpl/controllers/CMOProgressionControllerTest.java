package uk.gov.hmcts.reform.fpl.controllers;

import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("integration-test")
@WebMvcTest(CMOProgressionController.class)
@OverrideAutoConfiguration(enabled = true)
public class CMOProgressionControllerTest {

}
