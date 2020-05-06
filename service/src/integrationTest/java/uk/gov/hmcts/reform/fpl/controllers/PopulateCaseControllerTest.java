package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(PopulateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class PopulateCaseControllerTest extends AbstractControllerTest {

    private static final String URL_TEMPLATE = "/populateCase/%s/%s";
    private static final long CASE_ID = 1L;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    private Document document = document();

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    PopulateCaseControllerTest() {
        super("populate-case");
    }

    @BeforeEach
    void setup() {
        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document);
    }

    @Test
    void shouldReturnErrorForInvalidState() throws Exception {
        var result = makePostRequest("NOT_A_REAL_STATE", Map.of());

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    void shouldAddTimeBasedAndDocumentData() throws Exception {
        Map<String, Object> caseData = Map.of("updateTimeBasedAndDocumentData", "true", "property", "value");
        Map<String, Object> expectedCaseDataForUpdate = new HashMap<>(caseData);
        expectedCaseDataForUpdate.putAll(getExpectedTimeBasedAndDocumentData());
        expectedCaseDataForUpdate.remove("updateTimeBasedAndDocumentData");

        var result = makePostRequest("SUBMITTED", caseData);

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            "populateCase-Submitted",
            expectedCaseDataForUpdate);
    }

    @Test
    void shouldAddSDODataForPrepareForHearingState() throws Exception {
        Map<String, Object> caseData = Map.of("updateTimeBasedAndDocumentData", "true",
            "standardDirectionOrder", Map.of());
        Map<String, Object> expectedCaseDataForUpdate = new HashMap<>(caseData);
        expectedCaseDataForUpdate.putAll(getExpectedTimeBasedAndDocumentData());
        expectedCaseDataForUpdate.put("standardDirectionOrder", getExpectedSDOData());
        expectedCaseDataForUpdate.remove("updateTimeBasedAndDocumentData");

        var result = makePostRequest("PREPARE_FOR_HEARING", caseData);

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            "populateCase-PREPARE_FOR_HEARING",
            expectedCaseDataForUpdate);
    }

    @Test
    void shouldNotAddAnyDataWhenUpdateTimeBasedAndDocumentDataIsNotSet() throws Exception {
        Map<String, Object> caseData = Map.of("property", "value");
        var result = makePostRequest("GATEKEEPING", caseData);

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            "populateCase-Gatekeeping",
            caseData);
    }

    private Map<String, Object> getExpectedTimeBasedAndDocumentData() {
        var expectedSubmittedForm = DocumentReference.builder()
            .filename("mockSubmittedApplication.pdf")
            .url("fakeUrl")
            .binaryUrl("fakeBinaryUrl")
            .build();
        var expectedDocument = Map.of(
            "documentStatus",
            "Attached",
            "typeOfDocument",
            DocumentReference.builder().filename("mockFile.txt").url("fakeUrl").binaryUrl("fakeBinaryUrl").build());

        return Map.of(
        "dateAndTimeSubmitted", now().toString(),
        "dateSubmitted", dateNow().toString(),
        "submittedForm", expectedSubmittedForm,
        "documents_checklist_document", expectedDocument,
        "documents_threshold_document", expectedDocument,
        "documents_socialWorkCarePlan_document", expectedDocument,
        "documents_socialWorkAssessment_document", expectedDocument,
        "documents_socialWorkEvidenceTemplate_document", expectedDocument
        );
    }

    private Map<String, Object> getExpectedSDOData() {
        return Map.of(
            "orderDoc", DocumentReference.builder()
                .filename("mockSDO.pdf")
                .url("fakeUrl")
                .binaryUrl("fakeBinaryUrl")
                .build()
        );

    }

    private MvcResult makePostRequest(String state, Map<String, Object> body) throws Exception {
        return mockMvc
            .perform(post(String.format(URL_TEMPLATE, CASE_ID, state))
                .header("authorization", USER_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body))
            ).andReturn();
    }

}
