package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validation.groups.UploadDocumentsGroup;

import java.util.List;
import javax.validation.groups.Default;

@Component
public class DocumentsValidator extends PropertiesValidator {

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
}
