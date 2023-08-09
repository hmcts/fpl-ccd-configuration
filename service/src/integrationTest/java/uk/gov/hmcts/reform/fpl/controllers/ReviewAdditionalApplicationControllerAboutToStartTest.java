package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ReviewAdditionalApplicationService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithRandomUUID;

@WebMvcTest(ReviewAdditionalApplicationController.class)
@OverrideAutoConfiguration(enabled = true)
public class ReviewAdditionalApplicationControllerAboutToStartTest extends AbstractCallbackTest {
    @MockBean
    private ReviewAdditionalApplicationService reviewAdditionalApplicationService;

    private static final Map<String, Object> initFieldMap = Map.of(
        "hasApplicationToBeReviewed", YesNo.YES,
        "onlyOneApplicationToBeReviewed", NO,
        "additionalApplicationToBeReviewedList",
        asDynamicList(wrapElementsWithRandomUUID(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder().uploadedDateTime("1 January 2021, 12:00pm").build())
                .build(),
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder().uploadedDateTime("1 January 2021, 3:00pm").build())
                .build()
        ), AdditionalApplicationsBundle::toLabel));

    ReviewAdditionalApplicationControllerAboutToStartTest() {
        super("review-additional-application");
    }

    @BeforeEach
    void initTest() {
        when(reviewAdditionalApplicationService.initEventField(any())).thenReturn(initFieldMap);
    }

    //@Test
    void shouldInitEventField() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(CaseData.builder().build());
        CaseData resultCaseData = extractCaseData(response);
        ConfirmApplicationReviewedEventData resultEventData = resultCaseData.getConfirmApplicationReviewedEventData();

        assertThat(resultEventData.getHasApplicationToBeReviewed())
            .isEqualTo(initFieldMap.get("hasApplicationToBeReviewed"));
        assertThat(resultEventData.getOnlyOneApplicationToBeReviewed())
            .isEqualTo(initFieldMap.get("onlyOneApplicationToBeReviewed"));
        assertThat(resultEventData.getAdditionalApplicationToBeReviewedList())
            .isEqualTo(initFieldMap.get("additionalApplicationToBeReviewedList"));
    }

}
