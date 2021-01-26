package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.orders.ReviewCMOController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        DocumentReference orderForFirstCMO = testDocumentReference();
        DocumentReference orderForSecondCMO = testDocumentReference();
        UUID firstDraftCMOId = UUID.randomUUID();

        List<Element<HearingOrder>> draftedCMOs = List.of(
            element(firstDraftCMOId, buildCMO(orderForFirstCMO, "Case management hearing 25th December 2020")),
            element(buildCMO(orderForSecondCMO, "Issue resolution hearing 1st January 2021"))
        );


        CaseDetails caseDetails = CaseDetails.builder().data(
            Map.of("draftUploadedCMOs",
                draftedCMOs,
                "cmoToReviewList", firstDraftCMOId.toString()
            )).build();

        CaseData responseData = extractCaseData(postMidEvent(caseDetails));

        ReviewDecision expectedPageData = ReviewDecision.builder()
            //.hearing("Case management hearing 25th December 2020")
            //.document(orderForFirstCMO)
            .build();

        assertThat(responseData.getReviewCMODecision()).isEqualTo(expectedPageData);

    }

    private static HearingOrder buildCMO(DocumentReference order, String hearing) {
        return HearingOrder.builder()
            .hearing(hearing)
            .order(order)
            .status(SEND_TO_JUDGE).build();
    }
}
