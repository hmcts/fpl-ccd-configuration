package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@WebMvcTest(NoticeOfProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfProceedingsSubmittedControllerTest extends AbstractCallbackTest {

    NoticeOfProceedingsSubmittedControllerTest() {
        super("notice-of-proceedings");
    }

    @Test
    @Deprecated
    void testSubmit() {
        postSubmittedEvent(callbackRequest().getCaseDetails());
    }

}
