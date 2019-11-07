package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasDocumentsIncludedInSwet;
import uk.gov.hmcts.reform.fpl.validators.models.SwetDocuments;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasDocumentsIncludedInSwetValidator implements ConstraintValidator<HasDocumentsIncludedInSwet, CaseData> {
    private boolean valid = true;

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        getFormattedSwetDocuments(caseData).forEach(documentMap -> {
            Document document = documentMap.getDocument();
            if (document.getDocumentStatus().equals("Included in social work evidence template (SWET)")
                && (!hasAttachedSwet(caseData.getSocialWorkEvidenceTemplateDocument()))) {
                constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext
                    .getDefaultConstraintMessageTemplate())
                    .addPropertyNode(documentMap.getKey()).addConstraintViolation();
                constraintValidatorContext.disableDefaultConstraintViolation();

                valid = false;
            }
        });

        return valid;
    }

    private boolean hasAttachedSwet(Document socialWorkEvidence) {
        if (socialWorkEvidence == null) {
            return false;
        }

        return (socialWorkEvidence.getDocumentStatus() != null
            && socialWorkEvidence.getDocumentStatus().equals("Attached")
            && socialWorkEvidence.getTypeOfDocument() != null);
    }

    private List<SwetDocuments> getFormattedSwetDocuments(CaseData caseData) {
        List<SwetDocuments> mandatoryDocuments = new ArrayList<>();

        mandatoryDocuments.add(SwetDocuments.builder()
            .key("socialWorkCarePlanDocument")
            .document(caseData.getSocialWorkCarePlanDocument())
            .build());

        mandatoryDocuments.add(SwetDocuments.builder()
            .key("socialWorkStatementDocument")
            .document(caseData.getSocialWorkStatementDocument())
            .build());

        mandatoryDocuments.add(SwetDocuments.builder()
            .key("socialWorkAssessmentDocument")
            .document(caseData.getSocialWorkAssessmentDocument())
            .build());

        mandatoryDocuments.add(SwetDocuments.builder()
            .key("socialWorkChronologyDocument")
            .document(caseData.getSocialWorkChronologyDocument())
            .build());

        mandatoryDocuments.add(SwetDocuments.builder()
            .key("checklistDocument")
            .document(caseData.getChecklistDocument())
            .build());

        mandatoryDocuments.add(SwetDocuments.builder()
            .key("thresholdDocument")
            .document(caseData.getThresholdDocument())
            .build());

        return mandatoryDocuments.stream()
            .filter(documentsValidationMap -> documentsValidationMap.getDocument() != null
                && documentsValidationMap.getDocument().getDocumentStatus() != null)
            .collect(Collectors.toList());
    }
}
