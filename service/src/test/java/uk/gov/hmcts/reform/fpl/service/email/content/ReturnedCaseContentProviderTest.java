package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons;
import uk.gov.hmcts.reform.fpl.exceptions.DocumentException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate.ReturnedCaseTemplateBuilder;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {ReturnedCaseContentProvider.class, LookupTestConfig.class})
class ReturnedCaseContentProviderTest extends AbstractEmailContentProviderTest {

    private static final byte[] C110A_BINARY = DOCUMENT_CONTENT;
    private static final String ENCODED_BINARY = Base64.getEncoder().encodeToString(C110A_BINARY);
    private static final DocumentReference C110A = testDocumentReference();
    private static final String FAMILY_MAN_CASE_NUMBER = "FAM_NUM";
    private static final String CHILD_LAST_NAME = "Jones";
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(12345L)
        .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
        .localAuthorities(wrapElementsWithUUIDs(LocalAuthority.builder()
            .name(LOCAL_AUTHORITY_1_NAME)
            .id(LOCAL_AUTHORITY_1_CODE)
            .designated(YES.getValue())
            .email(LOCAL_AUTHORITY_1_INBOX)
            .build()))
        .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
        .children1(wrapElements(mock(Child.class)))
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().firstName("Paul").lastName("Smith").build())
            .build()))
        .c110A(uk.gov.hmcts.reform.fpl.model.group.C110A.builder()
            .submittedForm(C110A)
            .build())
        .returnApplication(ReturnApplication.builder()
            .reason(List.of(ReturnedApplicationReasons.INCOMPLETE, ReturnedApplicationReasons.CLARIFICATION_NEEDED))
            .note("Missing children details")
            .build())
        .build();

    @Autowired
    private ReturnedCaseContentProvider returnedCaseContentProvider;

    @MockBean
    private EmailNotificationHelper helper;

    @BeforeEach
    void setUp() {
        when(helper.getEldestChildLastName(CASE_DATA.getAllChildren())).thenReturn(CHILD_LAST_NAME);
    }

    @Test
    void shouldBuildReturnedCaseTemplateWithCaseUrlWithAllData() {
        ReturnedCaseTemplate expectedData = returnedCaseTemplateWithCaseUrl().build();

        ReturnedCaseTemplate actualData = returnedCaseContentProvider.parametersWithCaseUrl(CASE_DATA);

        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    void shouldBuildReturnedCaseTemplateWithCaseUrlWithAllDataWhenToggleEnabled() {
        ReturnedCaseTemplate expectedData = returnedCaseTemplateWithCaseUrl().build();
        ReturnedCaseTemplate actualData = returnedCaseContentProvider.parametersWithCaseUrl(CASE_DATA);
        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    void shouldBuildReturnedCaseTemplateWithCaseUrlWithoutOptionalData() {
        CaseData caseData = CASE_DATA.toBuilder().familyManCaseNumber(null).build();

        ReturnedCaseTemplate expectedData = returnedCaseTemplateWithCaseUrl()
            .familyManCaseNumber("")
            .build();

        ReturnedCaseTemplate actualData = returnedCaseContentProvider.parametersWithCaseUrl(caseData);

        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    void shouldBuildReturnedCaseTemplateWithApplicationUrl() {
        ReturnedCaseTemplate expectedData = returnedCaseTemplateWithApplicationUrl().build();

        when(documentDownloadService.downloadDocument(C110A.getBinaryUrl())).thenReturn(C110A_BINARY);

        ReturnedCaseTemplate actualData = returnedCaseContentProvider.parametersWithApplicationLink(CASE_DATA);

        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    void shouldThrowExceptionWhenDocumentCannotBeDownload() {
        when(documentDownloadService.downloadDocument(any())).thenReturn(null);

        assertThrows(
            DocumentException.class, () -> returnedCaseContentProvider.parametersWithApplicationLink(CASE_DATA)
        );
    }

    @Test
    void shouldThrowExceptionWhenApplicationDocumentIsEmpty() {
        when(documentDownloadService.downloadDocument(any())).thenReturn(new byte[0]);

        assertThrows(
            DocumentException.class, () -> returnedCaseContentProvider.parametersWithApplicationLink(CASE_DATA)
        );
    }

    @Test
    void shouldThrowExceptionWhenDocumentIsNotSpecified() {
        CaseData caseData = CASE_DATA.toBuilder()
            .c110A(uk.gov.hmcts.reform.fpl.model.group.C110A.builder()
                .submittedForm(null)
                .build())
            .build();

        assertThrows(
            DocumentException.class,
            () -> returnedCaseContentProvider.parametersWithApplicationLink(caseData)
        );
    }

    private ReturnedCaseTemplateBuilder returnedCaseTemplateWithCaseUrl() {
        return returnedCaseTemplate().caseUrl(caseUrl(CASE_REFERENCE));
    }

    private ReturnedCaseTemplateBuilder returnedCaseTemplateWithApplicationUrl() {
        return returnedCaseTemplate().applicationDocumentUrl(Map.of("file", ENCODED_BINARY, "is_csv", false));
    }

    private ReturnedCaseTemplateBuilder returnedCaseTemplate() {
        return ReturnedCaseTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .lastName(CHILD_LAST_NAME)
            .respondentFullName("Paul Smith")
            .returnedReasons("Application incomplete, clarification needed")
            .returnedNote("Missing children details")
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER);
    }
}
