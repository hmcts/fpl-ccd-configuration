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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(NoticeOfProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfProceedingsControllerAboutToSubmitTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String C6_DOCUMENT_TITLE = C6.getDocumentTitle();
    private static final byte[] PDF = {1, 2, 3, 4, 5};

    @MockBean
    private NoticeOfProceedingsService noticeOfProceedingsService;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGenerateC6NoticeOfProceedingsDocument() throws Exception {
        Document document = document();
        DocmosisDocument docmosisDocument = DocmosisDocument.builder()
            .bytes(PDF)
            .documentTitle(C6_DOCUMENT_TITLE)
            .build();

        CaseData caseData = mapper.convertValue(callbackRequest().getCaseDetails().getData(), CaseData.class);

        Map<String, Object> templateData = createTemplatePlaceholders();

        given(noticeOfProceedingsService.getNoticeOfProceedingTemplateData(caseData))
            .willReturn(templateData);
        given(docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, C6))
            .willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(USER_ID, AUTH_TOKEN, PDF, C6_DOCUMENT_TITLE))
            .willReturn(document);

        MvcResult response = makeRequest(callbackRequest());

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(responseCaseData.getNoticeOfProceedingsBundle()).hasSize(1);

        DocumentReference noticeOfProceedingBundle = responseCaseData.getNoticeOfProceedingsBundle().get(0).getValue()
            .getDocument();

        assertThat(noticeOfProceedingBundle.getUrl()).isEqualTo(document.links.self.href);
        assertThat(noticeOfProceedingBundle.getFilename()).isEqualTo(document.originalDocumentName);
        assertThat(noticeOfProceedingBundle.getBinaryUrl()).isEqualTo(document.links.binary.href);
    }

    private MvcResult makeRequest(CallbackRequest request) throws Exception {
        return mockMvc
            .perform(post("/callback/notice-of-proceedings/about-to-submit")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
    }

    private Map<String, Object> createTemplatePlaceholders() {
        return Map.of(
            "courtName", "Swansea Family Court",
            "familyManCaseNumber", "SW123123",
            "applicantName", "James Nelson",
            "orderTypes", "Care order",
            "childrenNames", "James Nelson",
            "hearingDate", "1 Jan 2001",
            "hearingVenue", "Aldgate Tower floor 3",
            "preHearingAttendance", "test",
            "hearingTime", "09.00pm"
        );
    }
}
