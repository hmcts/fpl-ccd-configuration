package uk.gov.hmcts.reform.fpl.service.casesubmission;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C110A;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseSubmissionService  {
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final CaseSubmissionTemplateDataGenerationService documentGenerationService;

    public Document generateSubmittedFormPDF(final CaseData caseData, final String pdfFileName)
            throws IOException {
        DocmosisCaseSubmission submittedCase = documentGenerationService.getTemplateData(caseData);

        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(
            submittedCase, C110A);

        return uploadDocumentService.uploadPDF(document.getBytes(), pdfFileName);
    }
}
