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
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ConfirmApplicationReviewedService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ConfirmApplicationReviewedController.class)
@OverrideAutoConfiguration(enabled = true)
public class ConfirmApplicationReviewedControllerAboutToSubmitTest extends AbstractCallbackTest {
    @MockBean
    private ConfirmApplicationReviewedService confirmApplicationReviewedService;

    private static final List<Element<AdditionalApplicationsBundle>> APPLICATION_BUNDLE_ELEMENT_LIST =
        wrapElements(AdditionalApplicationsBundle.builder()
            .uploadedDateTime("1 January 2021, 12:00pm")
            .author("TESTING")
            .applicationReviewed(YesNo.YES)
            .build());

    ConfirmApplicationReviewedControllerAboutToSubmitTest() {
        super("confirm-additional-application-reviewed");
    }

    @BeforeEach
    void initTest() {
        when(confirmApplicationReviewedService.markSelectedBundleAsReviewed(any()))
            .thenReturn(APPLICATION_BUNDLE_ELEMENT_LIST);
    }

    @Test
    void shouldUpdateAdditionalApplicationBundles() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(CaseData.builder().build());
        CaseData resultCaseData = extractCaseData(response);

        assertThat(resultCaseData.getAdditionalApplicationsBundle())
            .isEqualTo(APPLICATION_BUNDLE_ELEMENT_LIST);
    }

}
