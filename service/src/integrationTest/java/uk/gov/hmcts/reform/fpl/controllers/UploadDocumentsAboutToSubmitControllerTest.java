package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ApplicationDocumentsService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.THRESHOLD;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
public class UploadDocumentsAboutToSubmitControllerTest extends AbstractControllerTest {
    @MockBean
    private ApplicationDocumentsService applicationDocumentsService;

    @MockBean
    private FeatureToggleService featureToggleService;

    UploadDocumentsAboutToSubmitControllerTest() {
        super("upload-documents");
    }

    private static final String USER = "kurt@swansea.gov.uk";
    private static final CaseDetails caseDetails = callbackRequest().getCaseDetails();
    private static final CaseDetails caseDetailsBefore = callbackRequest().getCaseDetailsBefore();
    private static final LocalDate date = LocalDate.of(2021,10, 13);

    @BeforeEach
    void init() {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseData caseDataBefore = mapper.convertValue(caseDetailsBefore.getData(), CaseData.class);

        given(applicationDocumentsService.updateApplicationDocuments(caseData.getApplicationDocuments(),
            caseDataBefore.getApplicationDocuments()))
            .willReturn(getUpdatedCaseData());
    }

    @Test
    void shouldUpdateDocumentsInCaseDataWithCreatedByAndDateTimeUploadedFieldsIncludedWhenApplicationDocumentEventToggledOn() {
        given(featureToggleService.isApplicationDocumentsEventEnabled()).willReturn(true);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);

        ApplicationDocument expectedDocument = ApplicationDocument.builder()
            .documentType(THRESHOLD)
            .document(getExpectedDocumentReference())
            .uploadedBy(USER)
            .dateTimeUploaded(LocalDateTime.of(date, LocalTime.of(13, 30)))
            .build();

        CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        ApplicationDocument actualDocument = responseCaseData.getApplicationDocuments().get(0).getValue();

        assertThat(actualDocument).isEqualTo(expectedDocument);
    }

    private Map<String, Object> getUpdatedCaseData() {
        Map<String, Object> updatedCaseData = new HashMap<>();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<ApplicationDocument>> updatedDocuments = caseData.getApplicationDocuments();
        updatedDocuments.get(0).getValue().setUploadedBy(USER);
        updatedDocuments.get(0).getValue().setDateTimeUploaded(LocalDateTime.of(date,
            LocalTime.of(13, 30)));
        updatedCaseData.put("applicationDocuments", updatedDocuments);

        return updatedCaseData;
    }

    private DocumentReference getExpectedDocumentReference() {
        return DocumentReference.builder()
            .filename("solicitor-role-tech.docx")
            .binaryUrl("http://dm-store:8080/documents/17dfcfa7-13a3-4257-8582-3b85a756160b/binary")
            .url("http://dm-store:8080/documents/17dfcfa7-13a3-4257-8582-3b85a756160b")
            .build();
    }
}
