package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerAboutToStartTest extends AbstractCallbackTest {

    @MockBean
    private RepresentativeService representativeService;

    ChildControllerAboutToStartTest() {
        super("enter-children");
    }

    @Test
    void aboutToStartShouldPrepopulateChildrenDataWhenNoChildExists() {
        when(representativeService.shouldUserHaveAccessToRespondentsChildrenEvent(any())).thenReturn(true);

        CaseData caseData = CaseData.builder().build();
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData);

        assertThat(callbackResponse.getData()).containsKey("children1");
        assertThat(callbackResponse.getErrors()).isNullOrEmpty();
    }

    @Test
    void shouldThrowErrorIfUserRestrictedFromAccessingEvent() {
        when(representativeService.shouldUserHaveAccessToRespondentsChildrenEvent(any())).thenReturn(false);
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getErrors())
            .contains("Contact the applicant or CTSC to modify children details.");
    }

}
