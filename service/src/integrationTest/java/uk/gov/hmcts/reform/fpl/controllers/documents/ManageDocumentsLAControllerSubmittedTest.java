package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsLAController.class)
@OverrideAutoConfiguration(enabled = true)
public class ManageDocumentsLAControllerSubmittedTest extends AbstractControllerTest {
    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private EventService eventPublisher;

    ManageDocumentsLAControllerSubmittedTest() {
        super("manage-documents-la");
    }

    @Test
    void shouldNotPublishEventWhenUploadNotificationFeatureIsDisabled() {
        when(featureToggleService.isFurtherEvidenceUploadNotificationEnabled()).thenReturn(false);
        postSubmittedEvent(buildDummyCallbackRequest());
        verify(eventPublisher, never()).publishEvent(any(FurtherEvidenceUploadedEvent.class));
    }

    @Test
    void shouldPublishEventWhenUploadNotificationFeatureIsEnabled() {
        when(featureToggleService.isFurtherEvidenceUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        postSubmittedEvent(buildDummyCallbackRequest());
        verify(eventPublisher).publishEvent(any(FurtherEvidenceUploadedEvent.class));
    }

    private static CallbackRequest buildDummyCallbackRequest() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("dummy", "some dummy data"))
            .build();

        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
    }
}
