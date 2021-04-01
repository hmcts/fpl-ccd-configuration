package uk.gov.hmcts.reform.fpl.service.email.content;

import org.json.JSONObject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.DocumentException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate.ReturnedCaseTemplateBuilder;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;

@ContextConfiguration(classes = {ReturnedCaseContentProvider.class, LookupTestConfig.class})
class ReturnedCaseContentProviderTest extends AbstractEmailContentProviderTest {

    private static final byte[] APPLICATION_BINARY = TestDataHelper.DOCUMENT_CONTENT;

    @Autowired
    private ReturnedCaseContentProvider returnedCaseContentProvider;

    @Test
    void shouldBuildReturnedCaseTemplateWithCaseUrlWithAllData() {
        CaseData caseDetails = populatedCaseData();
        ReturnedCaseTemplate expectedData = returnedCaseTemplateWithCaseUrl().build();

        ReturnedCaseTemplate actualData = returnedCaseContentProvider.parametersWithCaseUrl(caseDetails);

        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    void shouldBuildReturnedCaseTemplateWithCaseUrlWithoutOptionalData() {
        String familyManCaseNumber = "";
        CaseData caseData = populatedCaseData(Map.of("familyManCaseNumber", familyManCaseNumber));

        ReturnedCaseTemplate expectedData = returnedCaseTemplateWithCaseUrl()
            .familyManCaseNumber(familyManCaseNumber)
            .build();

        ReturnedCaseTemplate actualData = returnedCaseContentProvider.parametersWithCaseUrl(caseData);

        assertThat(actualData).isEqualTo(expectedData);
    }

    @Nested
    class TemplateWithApplicationLink {

        @Test
        void shouldBuildReturnedCaseTemplateWithApplicationUrl() {
            DocumentReference application = TestDataHelper.testDocumentReference();
            CaseData caseData = populatedCaseData(Map.of("applicationBinaryUrl", application.getBinaryUrl()));

            ReturnedCaseTemplate expectedData = returnedCaseTemplateWithApplicationUrl().build();

            when(documentDownloadService.downloadDocument(application.getBinaryUrl())).thenReturn(APPLICATION_BINARY);

            ReturnedCaseTemplate actualData = returnedCaseContentProvider.parametersWithApplicationLink(caseData);

            assertThat(actualData).isEqualTo(expectedData);
        }

        @Test
        void shouldThrowExceptionWhenDocumentCannotBeDownload() {
            CaseData caseData = populatedCaseData();

            when(documentDownloadService.downloadDocument(any())).thenReturn(null);

            assertThrows(DocumentException.class,
                () -> returnedCaseContentProvider.parametersWithApplicationLink(caseData));
        }

        @Test
        void shouldThrowExceptionWhenApplicationDocumentIsEmpty() {
            CaseData caseData = populatedCaseData();
            when(documentDownloadService.downloadDocument(any())).thenReturn(new byte[0]);
            assertThrows(DocumentException.class,
                () -> returnedCaseContentProvider.parametersWithApplicationLink(caseData));
        }

        @Test
        void shouldThrowExceptionWhenDocumentIsNotSpecified() {
            CaseData caseData = populatedCaseData().toBuilder()
                .submittedForm(null)
                .build();

            assertThrows(DocumentException.class,
                () -> returnedCaseContentProvider.parametersWithApplicationLink(caseData));
        }
    }

    private ReturnedCaseTemplateBuilder returnedCaseTemplateWithCaseUrl() {
        return returnedCaseTemplate().caseUrl(caseUrl(CASE_REFERENCE));
    }

    private ReturnedCaseTemplateBuilder returnedCaseTemplateWithApplicationUrl() {
        return returnedCaseTemplate()
            .applicationDocumentUrl(generateAttachedDocumentLink(APPLICATION_BINARY)
                .map(JSONObject::toMap)
                .orElse(null));
    }

    private ReturnedCaseTemplateBuilder returnedCaseTemplate() {
        return ReturnedCaseTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .respondentLastName("Smith")
            .respondentFullName("Paul Smith")
            .returnedReasons("Application incomplete, clarification needed")
            .returnedNote("Missing children details")
            .familyManCaseNumber("12345");
    }
}


