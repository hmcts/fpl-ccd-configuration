package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadC2DocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadC2DocumentsControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String USER_NAME = "Emma Taylor";

    private static final ZonedDateTime ZONE_DATE_TIME = ZonedDateTime.now(ZoneId.of("Europe/London"));
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("h:mma, d MMMM yyyy", Locale.UK);

    private static final String ERROR_MESSAGE = "You need to upload a file.";

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DateFormatterService dateFormatterService;

    @BeforeEach
    void before() {
        given(userDetailsService.getUserName(AUTH_TOKEN))
            .willReturn(USER_NAME);
    }

    @Test
    void shouldCreateC2DocumentBundle() throws Exception {
        Map<String,Object> data = createTemporaryC2Document();

        CallbackRequest request = createCallbackRequestWithTempC2Bundle(data);

        MvcResult response = performResponseCallBack(request, "about-to-submit");

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getTemporaryC2Document()).isNull();
        assertThat(caseData.getC2DocumentBundle()).hasSize(1);

        C2DocumentBundle uploadedC2DocumentBundle = caseData.getC2DocumentBundle().get(0).getValue();

        assertC2BundleDocument(uploadedC2DocumentBundle, "Test description");
        assertThat(uploadedC2DocumentBundle.getAuthor()).isEqualTo(USER_NAME);

        // updated to use LocalDate to avoid 1-minute issue
        LocalDateTime uploadedDateTime = LocalDateTime.parse(uploadedC2DocumentBundle.getUploadedDateTime(), FORMATTER);

        assertThat(dateFormatterService.formatLocalDateToString(uploadedDateTime.toLocalDate(), FormatStyle.MEDIUM))
            .isEqualTo(dateFormatterService.formatLocalDateToString(ZONE_DATE_TIME.toLocalDate(), FormatStyle.MEDIUM));
    }

    @Test
    void shouldAppendAnAdditionalC2DocumentBundleWhenAC2DocumentBundleIsPresent() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(callbackRequest().getCaseDetails())
            .build();

        MvcResult response = performResponseCallBack(request, "about-to-submit");

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getTemporaryC2Document()).isNull();
        assertThat(caseData.getC2DocumentBundle()).hasSize(2);

        C2DocumentBundle existingC2Document = caseData.getC2DocumentBundle().get(0).getValue();
        C2DocumentBundle appendedC2Document = caseData.getC2DocumentBundle().get(1).getValue();

        assertC2BundleDocument(existingC2Document, "C2 document one");
        assertC2BundleDocument(appendedC2Document, "C2 document two");

        assertThat(appendedC2Document.getAuthor()).isEqualTo(USER_NAME);

        // updated to use LocalDate to avoid 1-minute issue
        LocalDateTime uploadedDateTime = LocalDateTime.parse(appendedC2Document.getUploadedDateTime(), FORMATTER);

        assertThat(dateFormatterService.formatLocalDateToString(uploadedDateTime.toLocalDate(), FormatStyle.MEDIUM))
            .isEqualTo(dateFormatterService.formatLocalDateToString(ZONE_DATE_TIME.toLocalDate(), FormatStyle.MEDIUM));
    }

    @Test
    void midEventShouldNotReturnAnErrorWhenDocumentIsUploaded() throws Exception {
        Map<String,Object> data = createTemporaryC2Document();

        CallbackRequest request = createCallbackRequestWithTempC2Bundle(data);

        MvcResult response = performResponseCallBack(request, "mid-event");

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void midEventShouldReturnAnErrorWhenDocumentIsNotUploaded() throws Exception {
        Map<String, Object> data = ImmutableMap.of(
            "temporaryC2Document", ImmutableMap.of());

        CallbackRequest request = createCallbackRequestWithTempC2Bundle(data);

        MvcResult response = performResponseCallBack(request, "mid-event");

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    private void assertC2BundleDocument(C2DocumentBundle documentBundle, String description) throws IOException {
        Document document = document();

        assertThat(documentBundle.getDocument().getUrl()).isEqualTo(document.links.self.href);
        assertThat(documentBundle.getDocument().getFilename()).isEqualTo(document.originalDocumentName);
        assertThat(documentBundle.getDocument().getBinaryUrl()).isEqualTo(document.links.binary.href);
        assertThat(documentBundle.getDescription()).isEqualTo(description);
    }

    private CallbackRequest createCallbackRequestWithTempC2Bundle(Map<String, Object> data) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(data)
                .build())
            .build();
    }

    private Map<String,Object> createTemporaryC2Document() {
        return ImmutableMap.of(
            "temporaryC2Document", ImmutableMap.of(
                "document", ImmutableMap.of(
                    "document_url", "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4",
                    "document_binary_url",
                    "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary",
                    "document_filename", "file.pdf"),
                "description", "Test description"));
    }

    private MvcResult performResponseCallBack(CallbackRequest request, String endpoint) throws Exception {
        return mockMvc
            .perform(post("/callback/upload-c2/" + endpoint)
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
    }
}
