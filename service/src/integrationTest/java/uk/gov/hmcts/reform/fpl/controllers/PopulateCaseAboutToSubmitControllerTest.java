package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
class PopulateCaseAboutToSubmitControllerTest extends AbstractControllerTest {

    @MockBean
    private UploadDocumentService uploadDocumentService;

    private Document document = document();

    PopulateCaseAboutToSubmitControllerTest() {
        super("populate-case");
    }

    @BeforeEach
    void setup() {
        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document);
    }

    @Test
    void shouldReturnErrorIfFileDoesNotExist() {
        Map<String, Object> caseData = Map.of("caseDataFilename", "nonExistentFile");

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(buildCallbackRequest(caseData));

        assertThat(callbackResponse.getErrors()).containsOnly("Could not read file nonExistentFile");
        assertThat(callbackResponse.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldPopulateWithMandatorySubmissionFields() {
        Map<String, Object> caseData = Map.of("caseDataFilename", "mandatorySubmissionFields");

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(buildCallbackRequest(caseData));

        var responseData = callbackResponse.getData();
        assertThat(callbackResponse.getErrors()).isNull();
        assertCommonData(responseData);
        assertThat(responseData.get("state")).isEqualTo(State.SUBMITTED.getValue());
        //TODO: assertions for json data
    }

    @Test
    void shouldPopulateWithMandatoryWithMultipleChildrenFields() {
        Map<String, Object> caseData = Map.of("caseDataFilename", "mandatoryWithMultipleChildren");

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(buildCallbackRequest(caseData));

        var responseData = callbackResponse.getData();
        assertThat(callbackResponse.getErrors()).isNull();
        assertCommonData(responseData);
        assertThat(responseData.get("state")).isEqualTo(State.SUBMITTED.getValue());
        //TODO: assertions for json data
    }

    @Test
    void shouldPopulateWithGatekeepingFields() {
        Map<String, Object> caseData = Map.of("caseDataFilename", "gatekeeping");

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(buildCallbackRequest(caseData));

        var responseData = callbackResponse.getData();
        assertThat(callbackResponse.getErrors()).isNull();
        assertCommonData(responseData);
        assertThat(responseData.get("state")).isEqualTo(State.GATEKEEPING.getValue());
        //TODO: assertions for json data
    }

    private CallbackRequest buildCallbackRequest(Map<String, Object> caseData) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(RandomUtils.nextLong())
                .data(caseData)
                .build())
            .build();
    }

    private void assertCommonData(Map<String, Object> data) {
        var expectedDocumentReference = Map.of(
            "document_url", document.links.self.href,
            "document_binary_url", document.links.binary.href,
            "document_filename", document.originalDocumentName);
        var expectedDocument = Map.of(
            "documentStatus", "Attached",
            "typeOfDocument", expectedDocumentReference);

        assertThat(data.get("dateAndTimeSubmitted")).isEqualTo(now().toString());
        assertThat(data.get("dateSubmitted")).isEqualTo(dateNow().toString());
        assertThat(data.get("submittedForm")).isEqualTo(expectedDocumentReference);
        assertThat(data.get("documents_checklist_document")).isEqualTo(expectedDocument);
        assertThat(data.get("documents_threshold_document")).isEqualTo(expectedDocument);
        assertThat(data.get("documents_socialWorkCarePlan_document")).isEqualTo(expectedDocument);
        assertThat(data.get("documents_socialWorkAssessment_document")).isEqualTo(expectedDocument);
        assertThat(data.get("documents_socialWorkEvidenceTemplate_document")).isEqualTo(expectedDocument);
    }

}
