package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
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
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadC2DocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadC2DocumentsAboutToSubmitControllerTest extends AbstractControllerTest {
    private static final String USER_NAME = "Emma Taylor";

    private static final ZonedDateTime ZONE_DATE_TIME = ZonedDateTime.now(ZoneId.of("Europe/London"));
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("h:mma, d MMMM yyyy", Locale.UK);
    private static final Long CASE_ID = 12345L;
    @MockBean
    private UserDetailsService userDetailsService;

    UploadC2DocumentsAboutToSubmitControllerTest() {
        super("upload-c2");
    }

    @BeforeEach
    void before() {
        given(userDetailsService.getUserName(userAuthToken))
            .willReturn(USER_NAME);
    }

    @Test
    void shouldCreateC2DocumentBundle() throws Exception {
        Map<String, Object> data = createTemporaryC2Document();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(createCase(data));

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getTemporaryC2Document()).isNull();
        assertThat(caseData.getC2DocumentBundle()).hasSize(1);

        C2DocumentBundle uploadedC2DocumentBundle = caseData.getC2DocumentBundle().get(0).getValue();

        assertC2BundleDocument(uploadedC2DocumentBundle, "Test description");
        assertThat(uploadedC2DocumentBundle.getAuthor()).isEqualTo(USER_NAME);

        // updated to use LocalDate to avoid 1-minute issue
        LocalDateTime uploadedDateTime = LocalDateTime.parse(uploadedC2DocumentBundle.getUploadedDateTime(), FORMATTER);

        assertThat(DateFormatterService.formatLocalDateToString(uploadedDateTime.toLocalDate(), FormatStyle.MEDIUM))
            .isEqualTo(DateFormatterService.formatLocalDateToString(ZONE_DATE_TIME.toLocalDate(), FormatStyle.MEDIUM));
    }

    @Test
    void shouldAppendAnAdditionalC2DocumentBundleWhenAC2DocumentBundleIsPresent() throws Exception {
        CaseDetails caseDetails = callbackRequest().getCaseDetails();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

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

        assertThat(DateFormatterService.formatLocalDateToString(uploadedDateTime.toLocalDate(), FormatStyle.MEDIUM))
            .isEqualTo(DateFormatterService.formatLocalDateToString(ZONE_DATE_TIME.toLocalDate(), FormatStyle.MEDIUM));
    }

    private void assertC2BundleDocument(C2DocumentBundle documentBundle, String description) throws IOException {
        Document document = document();

        assertThat(documentBundle.getDocument().getUrl()).isEqualTo(document.links.self.href);
        assertThat(documentBundle.getDocument().getFilename()).isEqualTo(document.originalDocumentName);
        assertThat(documentBundle.getDocument().getBinaryUrl()).isEqualTo(document.links.binary.href);
        assertThat(documentBundle.getDescription()).isEqualTo(description);
    }

    private Map<String, Object> createTemporaryC2Document() {
        return Map.of(
                "c2ApplicationType", Map.of(
                    "type", "WITH_NOTICE"),
            "temporaryC2Document", Map.of(
                "document", Map.of(
                    "document_url", "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4",
                    "document_binary_url",
                    "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary",
                    "document_filename", "file.pdf"),
                "description", "Test description"));
    }

    private CaseDetails createCase(Map<String, Object> data) {
        return CaseDetails.builder()
            .data(data)
            .id(CASE_ID)
            .build();
    }
}
