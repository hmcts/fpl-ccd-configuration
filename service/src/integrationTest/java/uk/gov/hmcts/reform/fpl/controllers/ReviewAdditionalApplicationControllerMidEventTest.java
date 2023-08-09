package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ReviewAdditionalApplicationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(ReviewAdditionalApplicationController.class)
@OverrideAutoConfiguration(enabled = true)
public class ReviewAdditionalApplicationControllerMidEventTest extends AbstractCallbackTest {
    @MockBean
    private ReviewAdditionalApplicationService reviewAdditionalApplicationService;

    private static final Element<AdditionalApplicationsBundle> APPLICATION_BUNDLE_ELEMENT =
        element(AdditionalApplicationsBundle.builder()
            .uploadedDateTime("1 January 2021, 12:00pm")
            .author("TESTING")
            .build());

    ReviewAdditionalApplicationControllerMidEventTest() {
        super("review-additional-application");
    }

    @BeforeEach
    void initTest() {
        when(reviewAdditionalApplicationService.getSelectedApplicationsToBeReviewed(any()))
            .thenReturn(APPLICATION_BUNDLE_ELEMENT);
    }

    //@Test
    void shouldQuerySelectedAdditionalApplicationBundle() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseData.builder().build());
        CaseData resultCaseData = extractCaseData(response);
        ConfirmApplicationReviewedEventData resultEventData = resultCaseData.getConfirmApplicationReviewedEventData();

        assertThat(resultEventData.getAdditionalApplicationsBundleToBeReviewed())
            .isEqualTo(APPLICATION_BUNDLE_ELEMENT.getValue());
    }

}
