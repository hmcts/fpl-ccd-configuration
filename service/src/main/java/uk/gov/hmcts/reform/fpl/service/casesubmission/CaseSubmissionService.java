package uk.gov.hmcts.reform.fpl.service.casesubmission;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.components.NoticeOfChangeRespondentConverter;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeRespondent;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.CaseSubmissionGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C110A;
import static uk.gov.hmcts.reform.fpl.utils.SubmittedFormFilenameHelper.buildFileName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseSubmissionService {
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final CaseSubmissionGenerationService documentGenerationService;
    private final ObjectMapper mapper;
    private final NoticeOfChangeRespondentConverter noticeOfChangeRespondentConverter;

    public Document generateSubmittedFormPDF(final CaseData caseData, final boolean isDraft) {
        DocmosisCaseSubmission submittedCase = documentGenerationService.getTemplateData(caseData);

        documentGenerationService.populateCaseNumber(submittedCase, caseData.getId());
        documentGenerationService.populateDraftWaterOrCourtSeal(submittedCase, isDraft);

        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(submittedCase, C110A);

        return uploadDocumentService.uploadPDF(document.getBytes(), buildFileName(caseData, isDraft));
    }

    public CaseDetails setNoticeOfChangeRespondents(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (isNotEmpty(caseData.getRespondents1()) && isNotEmpty(caseData.getAllApplicants())) {
            Applicant firstApplicant = caseData.getAllApplicants().get(0).getValue();

            for (int i = 0; i < caseData.getRespondents1().size(); i++) {
                Element<Respondent> respondentElement = caseData.getRespondents1().get(i);

                NoticeOfChangeRespondent noticeOfChangeRespondent = noticeOfChangeRespondentConverter.convert(
                    respondentElement, firstApplicant, SolicitorRole.values()[i]);

                caseDetails.getData().put(String.format("respondent%d", i + 1), noticeOfChangeRespondent);
            }
        }

        return caseDetails;
    }
}
