package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadC2DocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadC2DocumentsControllerTest extends AbstractControllerTest {
    private static final String USER_NAME = "Emma Taylor";

    private static final ZonedDateTime ZONE_DATE_TIME = ZonedDateTime.now(ZoneId.of("Europe/London"));
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("h:mma, d MMMM yyyy", Locale.UK);
    private static final Long CASE_ID = 12345L;
    private static final String RESPONDENT_SURNAME = "Watson";
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final UserInfo USER_INFO_CAFCASS = UserInfo.builder().roles(UserRole.CAFCASS.getRoles()).build();

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private DateFormatterService dateFormatterService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private IdamApi idamApi;

    UploadC2DocumentsControllerTest() {
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

        assertThat(dateFormatterService.formatLocalDateToString(uploadedDateTime.toLocalDate(), FormatStyle.MEDIUM))
            .isEqualTo(dateFormatterService.formatLocalDateToString(ZONE_DATE_TIME.toLocalDate(), FormatStyle.MEDIUM));
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

        assertThat(dateFormatterService.formatLocalDateToString(uploadedDateTime.toLocalDate(), FormatStyle.MEDIUM))
            .isEqualTo(dateFormatterService.formatLocalDateToString(ZONE_DATE_TIME.toLocalDate(), FormatStyle.MEDIUM));
    }

    @Test
    void submittedEventShouldNotifyHmctsAdminWhenCtscToggleIsDisabled() throws Exception {
        given(idamApi.retrieveUserInfo(any())).willReturn(USER_INFO_CAFCASS);

        postSubmittedEvent(enableSendToCtscOnCaseDetails(NO));

        verify(notificationClient).sendEmail(
            eq(C2_UPLOAD_NOTIFICATION_TEMPLATE),
            eq("admin@family-court.com"),
            eq(expectedNotificationParams()),
            eq(CASE_ID.toString())
        );

        verify(notificationClient, never()).sendEmail(
            eq(C2_UPLOAD_NOTIFICATION_TEMPLATE),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            eq(expectedNotificationParams()),
            eq(CASE_ID.toString())
        );
    }

    @Test
    void submittedEventShouldNotifyCtscAdminWhenCtscToggleIsEnabled() throws Exception {
        given(idamApi.retrieveUserInfo(any())).willReturn(USER_INFO_CAFCASS);

        postSubmittedEvent(enableSendToCtscOnCaseDetails(YES));

        verify(notificationClient, never()).sendEmail(
            eq(C2_UPLOAD_NOTIFICATION_TEMPLATE),
            eq("admin@family-court.com"),
            eq(expectedNotificationParams()),
            eq(CASE_ID.toString())
        );

        verify(notificationClient).sendEmail(
            eq(C2_UPLOAD_NOTIFICATION_TEMPLATE),
            eq("FamilyPublicLaw+ctsc@gmail.com"), eq(expectedNotificationParams()),
            eq(CASE_ID.toString())
        );
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

    private CaseDetails enableSendToCtscOnCaseDetails(YesNo enableCtsc) {
        return createCase(ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .put("sendToCtsc", enableCtsc.getValue())
            .build());
    }

    private Map<String, Object> buildCommonNotificationParameters() {
        return Map.of(
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            "familyManCaseNumber", "12345",
            "respondents1", List.of(
                Map.of(
                    "value", Respondent.builder()
                        .party(RespondentParty.builder()
                            .lastName(RESPONDENT_SURNAME)
                            .build())
                        .build()))
        );
    }

    private Map<String, Object> expectedNotificationParams() {
        return Map.of(
            "reference", CASE_ID.toString(),
            "hearingDetailsCallout", String.format("%s, %s", RESPONDENT_SURNAME, CASE_ID.toString()),
            "subjectLine", String.format("%s, %s", RESPONDENT_SURNAME, CASE_ID.toString()),
            "caseUrl", "http://fake-url/case/PUBLICLAW/CARE_SUPERVISION_EPO/12345"
        );
    }

    private CaseDetails createCase(Map<String, Object> data) {
        return CaseDetails.builder()
            .data(data)
            .id(CASE_ID)
            .build();
    }
}
