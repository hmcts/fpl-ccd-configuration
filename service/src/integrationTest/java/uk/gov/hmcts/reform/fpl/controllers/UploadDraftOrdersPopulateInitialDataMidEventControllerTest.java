package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.orders.UploadDraftOrdersController;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(UploadDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadDraftOrdersPopulateInitialDataMidEventControllerTest extends AbstractUploadDraftOrdersControllerTest {

    @Test
    void shouldClearTransientDraftOrderNeedsReviewingField() {
        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(UploadDraftOrdersData.builder().build())
            .draftOrderNeedsReviewUploaded(YesNo.YES)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "populate-initial-data");

        assertThat(callbackResponse.getData()).doesNotContainKey("draftOrderNeedsReviewUploaded");
    }
}
