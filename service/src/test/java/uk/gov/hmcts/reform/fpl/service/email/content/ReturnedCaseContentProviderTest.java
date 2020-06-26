package uk.gov.hmcts.reform.fpl.service.email.content;

import org.json.JSONObject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.exceptions.DocumentException;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate.ReturnedCaseTemplateBuilder;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;

@ContextConfiguration(classes = {ReturnedCaseContentProvider.class, LookupTestConfig.class})
class ReturnedCaseContentProviderTest extends AbstractEmailContentProviderTest {

    private static final byte[] APPLICATION_BINARY = TestDataHelper.DOCUMENT_CONTENT;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @Autowired
    private ReturnedCaseContentProvider returnedCaseContentProvider;

    @Test
    void shouldBuildReturnedCaseTemplateWithCaseUrlWithAllData() {
        CaseDetails caseDetails = populatedCaseDetails();
        ReturnedCaseTemplate expectedTemplate = returnedCaseTemplateWithCaseUrl().build();

        ReturnedCaseTemplate actualTemplate = returnedCaseContentProvider.parametersWithCaseUrl(caseDetails);

        assertThat(actualTemplate).isEqualTo(expectedTemplate);
    }

    @Test
    void shouldBuildReturnedCaseTemplateWithCaseUrlWithoutOptionalData() {
        String familyManCaseNumber = "";
        CaseDetails caseDetails = populatedCaseDetails(Map.of("familyManCaseNumber", familyManCaseNumber));
        ReturnedCaseTemplate expectedTemplate = returnedCaseTemplateWithCaseUrl()
            .familyManCaseNumber(familyManCaseNumber)
            .build();

        ReturnedCaseTemplate actualTemplate = returnedCaseContentProvider.parametersWithCaseUrl(caseDetails);

        assertThat(actualTemplate).isEqualTo(expectedTemplate);
    }


    @Nested
    class TemplateWithApplicationLink {

        @Test
        void shouldBuildReturnedCaseTemplateWithApplicationUrl() {
            final DocumentReference applicationDocument = TestDataHelper.testDocumentReference();
            CaseDetails caseDetails = populatedCaseDetails(Map.of(
                "applicationBinaryUrl", applicationDocument.getBinaryUrl()
            ));
            ReturnedCaseTemplate expectedTemplate = returnedCaseTemplateWithApplicationUrl().build();

            when(documentDownloadService.downloadDocument(applicationDocument.getBinaryUrl()))
                .thenReturn(APPLICATION_BINARY);

            ReturnedCaseTemplate actualTemplate = returnedCaseContentProvider
                .parametersWithApplicationLink(caseDetails);

            assertThat(actualTemplate).isEqualTo(expectedTemplate);
        }

        @Test
        void shouldThrowExceptionWhenDocumentCannotBeDownload() {
            CaseDetails caseDetails = populatedCaseDetails();

            when(documentDownloadService.downloadDocument(any())).thenReturn(null);

            assertThrows(DocumentException.class,
                () -> returnedCaseContentProvider.parametersWithApplicationLink(caseDetails));
        }

        @Test
        void shouldThrowExceptionWhenApplicationDocumentIsEmpty() {
            CaseDetails caseDetails = populatedCaseDetails();
            when(documentDownloadService.downloadDocument(any())).thenReturn(new byte[0]);
            assertThrows(DocumentException.class,
                () -> returnedCaseContentProvider.parametersWithApplicationLink(caseDetails));
        }

        @Test
        void shouldThrowExceptionWhenDocumentIsNotSpecified() {
            CaseDetails caseDetails = populatedCaseDetails();
            caseDetails.getData().remove("submittedForm");

            assertThrows(DocumentException.class,
                () -> returnedCaseContentProvider.parametersWithApplicationLink(caseDetails));
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


