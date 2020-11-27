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
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ApplicationDocumentsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
public class UploadDocumentsAboutToSubmitControllerTest extends AbstractControllerTest {
    @MockBean
    private ApplicationDocumentsService applicationDocumentsService;

    UploadDocumentsAboutToSubmitControllerTest() {
        super("upload-documents");
    }

    private static final String USER = "kurt@swansea.gov.uk";
    private static final CaseDetails caseDetails = callbackRequest().getCaseDetails();
    private static final CaseDetails caseDetailsBefore = callbackRequest().getCaseDetailsBefore();
    private static final LocalDate date = LocalDate.of(2021,10, 13);

    @BeforeEach
    void init() {
        Map<String, Object> updatedCaseData = new HashMap<>();

        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<ApplicationDocument>> updatedDocuments = caseData.getApplicationDocuments();
        updatedDocuments.get(0).getValue().setUploadedBy(USER);
        updatedDocuments.get(0).getValue().setDateTimeUploaded(LocalDateTime.of(date,
            LocalTime.of(13, 30)));
        updatedCaseData.put("applicationDocuments", updatedDocuments);

        CaseData caseDataBefore = mapper.convertValue(caseDetailsBefore.getData(), CaseData.class);

        given(applicationDocumentsService.updateCaseDocuments(caseData.getApplicationDocuments(),
            caseDataBefore.getApplicationDocuments()))
            .willReturn(updatedCaseData);
    }

    @Test
    void shouldUpdateDocumentsInCaseDataWithCreatedByAndDateTimeUploadedFieldsIncluded() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);

        assertThat(callbackResponse.getData().get("applicationDocuments")).isEqualToComparingOnlyGivenFields(
            Document.builder()
            .uploadedBy(USER)
            .dateTimeUploaded(LocalDateTime.of(date,
                LocalTime.of(13, 30))));
    }
}
