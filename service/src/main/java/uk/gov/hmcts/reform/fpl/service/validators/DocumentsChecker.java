package uk.gov.hmcts.reform.fpl.service.validators;

import jakarta.validation.groups.Default;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validation.groups.UploadDocumentsGroup;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyNonEmpty;

@Component
public class DocumentsChecker extends PropertiesChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return super.validate(caseData, List.of(
                "otherSocialWorkDocuments",
                "socialWorkCarePlanDocument",
                "socialWorkStatementDocument",
                "socialWorkAssessmentDocument",
                "socialWorkChronologyDocument",
                "checklistDocument",
                "thresholdDocument",
                "socialWorkEvidenceTemplateDocument"),
                UploadDocumentsGroup.class, Default.class
        );
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return anyNonEmpty(
                caseData.getOtherSocialWorkDocuments(),
                caseData.getSocialWorkCarePlanDocument(),
                caseData.getSocialWorkStatementDocument(),
                caseData.getSocialWorkAssessmentDocument(),
                caseData.getSocialWorkChronologyDocument(),
                caseData.getChecklistDocument(),
                caseData.getThresholdDocument(),
                caseData.getSocialWorkEvidenceTemplateDocument());
    }

}
