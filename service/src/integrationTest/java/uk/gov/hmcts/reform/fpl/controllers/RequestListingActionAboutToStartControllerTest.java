package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.listing.RequestListingActionController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebMvcTest(RequestListingActionController.class)
@OverrideAutoConfiguration(enabled = true)
class RequestListingActionAboutToStartControllerTest extends AbstractCallbackTest {

    public static final String NON_WA_COURT_ERROR = "Cannot request listing actions in this court.";
    public static final LocalDateTime NOW = LocalDateTime.of(2020, 1, 1, 0, 0);
    public static final Court WA_COURT = Court.builder().code("WA").build();
    public static final Court NON_WA_COURT = Court.builder().code("NON-WA").build();

    @MockBean
    private FeatureToggleService featureToggleService;

    RequestListingActionAboutToStartControllerTest() {
        super("request-listing-action");
    }

    @BeforeEach
    void beforeEach() {
        given(featureToggleService.isCourtNotificationEnabledForWa(WA_COURT)).willReturn(false);
        given(featureToggleService.isCourtNotificationEnabledForWa(NON_WA_COURT)).willReturn(true);
    }

    @Test
    void shouldNotErrorIfInWaCourt() {
        CaseData caseData = CaseData.builder()
            .court(WA_COURT)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getErrors()).isNullOrEmpty();
    }

    @Test
    void shouldErrorIfNotInWaCourt() {
        CaseData caseData = CaseData.builder()
            .court(NON_WA_COURT)
            .build();

        AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToStartEvent(caseData);

        assertThat(actualResponse.getErrors()).containsExactly(NON_WA_COURT_ERROR);
    }

    @Test
    void shouldErrorIfNoCourt() {
        CaseData caseData = CaseData.builder()
            .build();

        AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToStartEvent(caseData);

        assertThat(actualResponse.getErrors()).containsExactly(NON_WA_COURT_ERROR);
    }

}
