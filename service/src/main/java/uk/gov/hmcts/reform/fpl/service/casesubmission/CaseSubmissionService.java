package uk.gov.hmcts.reform.fpl.service.casesubmission;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C110A;
import static uk.gov.hmcts.reform.fpl.utils.SubmittedFormFilenameHelper.buildFileName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseSubmissionService  {
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final CaseSubmissionTemplateDataGenerationService documentGenerationService;
    private final ObjectMapper mapper;

    public Document generateSubmittedFormPDF(final CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        DocmosisCaseSubmission submittedCase = documentGenerationService.getTemplateData(caseData);

        DocmosisCaseSubmission submittedCaseWithCaseNumber =
            populateDocmosisCaseSubmissionWithCaseNumber(submittedCase, caseDetails.getId());

        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(
            submittedCaseWithCaseNumber, C110A);

        return uploadDocumentService.uploadPDF(document.getBytes(), buildFileName(caseDetails));
    }

    private DocmosisCaseSubmission populateDocmosisCaseSubmissionWithCaseNumber(
        final DocmosisCaseSubmission submittedCase,
        final long caseNumber) {
        return submittedCase.toBuilder().caseNumber(String.valueOf(caseNumber)).build();
    }
}
