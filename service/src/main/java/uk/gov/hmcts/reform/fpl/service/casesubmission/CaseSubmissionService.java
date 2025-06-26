package uk.gov.hmcts.reform.fpl.service.casesubmission;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.CaseSubmissionGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C1;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C110A;
import static uk.gov.hmcts.reform.fpl.utils.SubmittedFormFilenameHelper.buildFileName;
import static uk.gov.hmcts.reform.fpl.utils.SubmittedFormFilenameHelper.buildGenericFileName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseSubmissionService {
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final CaseSubmissionGenerationService documentGenerationService;

    public Document generateC110aSubmittedFormPDF(final CaseData caseData, final boolean isDraft) {
        return generateSubmittedFormPDF(caseData, isDraft, C110A);
    }

    public Document generateC1SubmittedFormPDF(final CaseData caseData, final boolean isDraft) {
        return generateSubmittedFormPDF(caseData, isDraft, C1);
    }

    private Document generateSubmittedFormPDF(CaseData caseData, boolean isDraft, DocmosisTemplates template) {
        DocmosisCaseSubmission submittedCase = documentGenerationService.getTemplateData(caseData);

        documentGenerationService.populateCaseNumber(submittedCase, caseData.getId());
        documentGenerationService.populateDraftWaterOrCourtSeal(submittedCase, isDraft, caseData);
        Language applicationLanguage = Optional.ofNullable(caseData.getC110A().getLanguageRequirementApplication())
            .orElse(Language.ENGLISH);

        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(submittedCase,
            template,
            RenderFormat.PDF,
            applicationLanguage);

        return uploadDocumentService.uploadPDF(document.getBytes(), buildFileName(caseData, isDraft, template));
    }

    public Document generateC1SupplementPDF(final CaseData caseData, final boolean isDraft) {
        if (caseData.isSecureAccommodationOrderType()) {
            return generateSupplementPDF(caseData, isDraft, DocmosisTemplates.C20_SUPPLEMENT,
                documentGenerationService.getC20SupplementData(caseData, isDraft));
        } else if (caseData.isRefuseContactWithChildApplication()) {
            return generateSupplementPDF(caseData, isDraft, DocmosisTemplates.C14_SUPPLEMENT,
                documentGenerationService.getC14SupplementData(caseData, isDraft));
        } else if (caseData.isChildRecoveryOrder()) {
            return generateSupplementPDF(caseData, isDraft, DocmosisTemplates.C18_SUPPLEMENT,
                documentGenerationService.getC18SupplementData(caseData, isDraft));
        } else if (caseData.isContactWithChildInCareApplication()) {
            return generateSupplementPDF(caseData, isDraft, DocmosisTemplates.C15_SUPPLEMENT,
                documentGenerationService.getC15SupplementData(caseData, isDraft));
        } else if (caseData.isEducationSupervisionApplication()) {
            return generateSupplementPDF(caseData, isDraft, DocmosisTemplates.C17_SUPPLEMENT,
                documentGenerationService.getC17SupplementData(caseData, isDraft));
        } else {
            return generateSupplementPDF(caseData, isDraft, DocmosisTemplates.C16_SUPPLEMENT,
                documentGenerationService.getC16SupplementData(caseData, isDraft));
        }
    }

    public Document generateSupplementPDF(final CaseData caseData, final boolean isDraft, DocmosisTemplates template,
                                          DocmosisData supplementData) {
        Language applicationLanguage = Optional.ofNullable(caseData.getC110A().getLanguageRequirementApplication())
            .orElse(Language.ENGLISH);

        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(supplementData,
            template,
            RenderFormat.PDF,
            applicationLanguage);

        return uploadDocumentService.uploadPDF(document.getBytes(), buildGenericFileName(isDraft, template));

    }

    public String getSigneeName(CaseData caseData) {
        return documentGenerationService.getSigneeName(caseData);
    }

    public String generateCaseName(CaseData caseData) {
        String applicantNames = String.join(", ", caseData.getLocalAuthorities().stream()
            .map(localAuthority -> localAuthority.getValue().getName())
            .toList());

        String respondentNames = String.join(", ", caseData.getRespondents1().stream()
            .map(respondent -> respondent.getValue().getParty().getLastName())
            .distinct()
            .toList());

        return String.format("%s & %s", applicantNames, respondentNames);
    }
}
