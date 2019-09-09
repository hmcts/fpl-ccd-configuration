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
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.c6Document;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;

@ActiveProfiles("integration-test")
@WebMvcTest(NoticeOfProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfProceedingsControllerAboutToSubmitTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @MockBean
    private CaseDataExtractionService caseDataExtractionService;
    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    @MockBean
    private UploadDocumentService uploadDocumentService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGenerateC6NoticeOfProceedingsDocument() throws Exception {
        byte[] pdf = {1, 2, 3, 4, 5};
        Document document = c6Document();
        DocmosisDocument docmosisDocument = DocmosisDocument.builder()
            .bytes(pdf)
            .documentTitle("notice_of_proceedings_(C6)")
            .build();

        Map<String, String> templateData = createTemplatePlaceholders();

        given(caseDataExtractionService.getNoticeOfProceedingTemplateData(any(), any()))
            .willReturn(templateData);
        given(docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, C6))
            .willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(USER_ID, AUTH_TOKEN, pdf, "notice_of_proceedings_(C6).pdf"))
            .willReturn(document);

        MvcResult response = makeRequest();

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData caseData = MAPPER.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getNoticeOfProceedingsBundle().get(0).getValue().getDocument().getDocument_url()).isEqualTo(document.links.self.href);
        assertThat(caseData.getNoticeOfProceedingsBundle().get(0).getValue().getDocument().getDocument_filename()).isEqualTo(document.originalDocumentName);
        assertThat(caseData.getNoticeOfProceedingsBundle().get(0).getValue().getDocument().getDocument_binary_url()).isEqualTo(document.links.binary.href);
    }

    private MvcResult makeRequest() throws Exception {
        return mockMvc
            .perform(post("/callback/notice-of-proceedings/about-to-submit")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(readBytes("fixtures/C6CaseData.json")))
            .andExpect(status().isOk())
            .andReturn();
    }

    private Map<String, String> createTemplatePlaceholders() {
        return Map.of(
            "jurisdiction", "PUBLICLAW",
            "familyManCaseNumber", "SW123123",
            "todaysDate", LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
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
