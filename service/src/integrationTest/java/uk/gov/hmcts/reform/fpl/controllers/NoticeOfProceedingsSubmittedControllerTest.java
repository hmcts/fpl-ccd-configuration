package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(NoticeOfProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfProceedingsSubmittedControllerTest extends AbstractControllerTest {

    NoticeOfProceedingsSubmittedControllerTest() {
        super("notice-of-proceedings");
    }

    @Test
    @Deprecated
    void testSubmit() {
        postSubmittedEvent(callbackRequest().getCaseDetails());
    }

}
