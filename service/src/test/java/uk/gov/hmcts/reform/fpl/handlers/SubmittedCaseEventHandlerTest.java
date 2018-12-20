package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.handlers.PdfGenerationHandler.SubmittedFormFilenameHelper;
import uk.gov.hmcts.reform.fpl.service.CaseRepository;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
class SubmittedCaseEventHandlerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    @Captor
    private ArgumentCaptor<CaseDetails> caseDetailsCaptor;
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private UploadDocumentService uploadDocumentService;
    @Mock
    private CaseRepository caseRepository;
    @Mock
    private UserDetailsService userDetailsService;
    @InjectMocks
    private PdfGenerationHandler submittedCaseEventHandler;

    @Test
    void fileNameShouldContainCaseReferenceWhenNoCaseNameIsProvided() throws IOException {
        CaseDetails caseDetails = emptyCaseDetails();

        String fileName = SubmittedFormFilenameHelper.buildFileName(caseDetails);

        assertThat(fileName).isEqualTo("123.pdf");
    }

    @Test
    void fileNameShouldContainCaseTitleWhenProvided() throws IOException {
        CaseDetails caseDetails = populatedCaseDetails();

        String fileName = SubmittedFormFilenameHelper.buildFileName(caseDetails);

        assertThat(fileName).isEqualTo("test.pdf");
    }

    @Test
    void shouldUpdateCaseWithReferenceToUploadedSubmittedFormPDF() throws IOException {
        CallbackRequest request = callbackRequest();

        byte[] pdf = {1, 2, 3, 4};
        String fileName = request.getCaseDetails().getData().get("caseName") + ".pdf";
        Document document = document();

        given(documentGeneratorService.generateSubmittedFormPDF(request.getCaseDetails()))
            .willReturn(pdf);
        given(uploadDocumentService.uploadPDF(USER_ID, AUTH_TOKEN, pdf, fileName))
            .willReturn(document);

        submittedCaseEventHandler.handleCaseSubmission(new SubmittedCaseEvent(request, AUTH_TOKEN, USER_ID));

        verify(caseRepository).setSubmittedFormPDF(AUTH_TOKEN, USER_ID,
            Long.toString(request.getCaseDetails().getId()), document);
    }

    @Test
    void shouldPassUserFullNameToPDFGenerator() throws IOException {
        given(userDetailsService.getUserName(AUTH_TOKEN))
            .willReturn("Emma Taylor");

        submittedCaseEventHandler.handleCaseSubmission(new SubmittedCaseEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(documentGeneratorService).generateSubmittedFormPDF(caseDetailsCaptor.capture());

        assertThat(caseDetailsCaptor.getValue().getData())
            .containsEntry("userFullName", "Emma Taylor");
    }
}
