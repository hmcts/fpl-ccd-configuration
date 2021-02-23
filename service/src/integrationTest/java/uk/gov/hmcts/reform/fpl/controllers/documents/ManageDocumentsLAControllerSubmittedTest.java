package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsLAController.class)
@OverrideAutoConfiguration(enabled = true)
public class ManageDocumentsLAControllerSubmittedTest extends AbstractControllerTest {
    @Mock
    FeatureToggleService featureToggleService;

    @Spy
    private ApplicationEventPublisher applicationEventPublisher;

    ManageDocumentsLAControllerSubmittedTest() {
        super("manage-documents-la");
    }

    @Test
    void shouldNotPublishEventWhenUploadNotificationFeatureIsDisabled() {
        when(featureToggleService.isFurtherEvidenceUploadNotificationEnabled()).thenReturn(false);
        postSubmittedEvent(buildDummyCallbackRequest());
        verify(applicationEventPublisher, never()).publishEvent(any(FurtherEvidenceUploadedEvent.class));
    }

    @Test
    void shouldNotPublishEventWhenUploadNotificationFeatureIsEnabled() {
        when(featureToggleService.isFurtherEvidenceUploadNotificationEnabled()).thenReturn(true);
        postSubmittedEvent(buildDummyCallbackRequest());
        verify(applicationEventPublisher, never()).publishEvent(any(FurtherEvidenceUploadedEvent.class));
    }

    private static CallbackRequest buildDummyCallbackRequest() {
        CaseDetails caseDetails = CaseDetails.builder().build();

        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
    }
}
