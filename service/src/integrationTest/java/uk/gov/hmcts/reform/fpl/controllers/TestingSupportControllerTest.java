package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.NestedServletException;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.testingsupport.controllers.TestingSupportController;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@ActiveProfiles("integration-test")
@WebMvcTest(TestingSupportController.class)
@OverrideAutoConfiguration(enabled = true)
class TestingSupportControllerTest extends AbstractControllerTest {

    private static final String URL_TEMPLATE = "/testingSupport/populateCase/%s";
    private static final long CASE_ID = 1L;
    private static final DocumentReference.DocumentReferenceBuilder MOCK_DOCUMENT_BUILDER = DocumentReference.builder()
        .url("http://fake-document-management-store-api/documents/fakeUrl")
        .binaryUrl("http://fake-document-management-store-api/documents/fakeUrl/binary");

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    TestingSupportControllerTest() {
        super("populate-case");
    }

    @Test
    void shouldThrowExceptionForInvalidState() {
        NestedServletException thrownException = assertThrows(NestedServletException.class,
            () -> makePostRequest(Map.of("state", "NOT_A_REAL_STATE")));

        assertThat(thrownException.getMessage()).contains("No enum constant");
    }

    @Test
    void shouldAddTimeBasedAndDocumentData() throws Exception {
        Map<String, Object> caseData = Map.of("property", "value");
        Map<String, Object> requestBody = Map.of(
            "state", "SUBMITTED",
            "updateTimeBasedAndDocumentData", true,
            "data", caseData);
        Map<String, Object> expectedCaseDataForUpdate = new HashMap<>(caseData);
        expectedCaseDataForUpdate.putAll(getExpectedTimeBasedAndDocumentData());

        var result = makePostRequest(requestBody);

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
        Map<String, Object> caseData = Map.of("standardDirectionOrder", Map.of());
        Map<String, Object> requestBody = Map.of(
            "state", "PREPARE_FOR_HEARING",
            "updateTimeBasedAndDocumentData", true,
            "data", caseData);
        Map<String, Object> expectedCaseDataForUpdate = new HashMap<>(caseData);
        expectedCaseDataForUpdate.putAll(getExpectedTimeBasedAndDocumentData());
        expectedCaseDataForUpdate.put("standardDirectionOrder", getExpectedSDOData());

        var result = makePostRequest(requestBody);

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
        Map<String, Object> requestBody = Map.of(
            "state", "GATEKEEPING",
            "data", caseData);
        Map<String, Object> expectedCaseDataForUpdate = new HashMap<>(caseData);

        var result = makePostRequest(requestBody);

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            "populateCase-Gatekeeping",
            expectedCaseDataForUpdate);
    }

    private Map<String, Object> getExpectedTimeBasedAndDocumentData() {
        var expectedSubmittedForm = MOCK_DOCUMENT_BUILDER.filename("mockSubmittedApplication.pdf").build();
        var expectedDocument = Map.of("documentStatus",
            "Attached",
            "typeOfDocument",
            MOCK_DOCUMENT_BUILDER.filename("mockFile.txt").build());

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
        return Map.of("orderDoc", MOCK_DOCUMENT_BUILDER.filename("mockSDO.pdf").build());
    }

    private MvcResult makePostRequest(Map<String, Object> body) throws Exception {
        return mockMvc
            .perform(post(String.format(URL_TEMPLATE, CASE_ID))
                .header("authorization", USER_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body))
            ).andReturn();
    }

}
