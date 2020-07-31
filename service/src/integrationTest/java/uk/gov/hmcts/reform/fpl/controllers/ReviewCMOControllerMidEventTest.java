package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.cmo.ReviewCMOController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ActiveProfiles("integration-test")
@WebMvcTest(ReviewCMOController.class)
@OverrideAutoConfiguration(enabled = true)
class ReviewCMOControllerMidEventTest extends AbstractControllerTest {

    ReviewCMOControllerMidEventTest() {
        super("review-cmo");
    }

    @Test
    void shouldPopulateReviewDecisionPage() {
        DocumentReference order = testDocumentReference();

        CaseDetails caseDetails = CaseDetails.builder().data(
            Map.of("draftUploadedCMOs", List.of(element(buildCMO(order))))).build();

        CaseData responseData = extractCaseData(postMidEvent(caseDetails));

        ReviewDecision expectedPageData = ReviewDecision.builder()
            .hearing("Test hearing 25th December 2020")
            .document(order)
            .build();

        assertThat(responseData.getReviewCMODecision()).isEqualTo(expectedPageData);

    }

    private static CaseManagementOrder buildCMO(DocumentReference order) {
        return CaseManagementOrder.builder()
            .hearing("Test hearing 25th December 2020")
            .order(order)
            .status(SEND_TO_JUDGE).build();
    }
}
