package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.validation.groups.UploadDocumentsGroup;

import javax.validation.groups.Default;

@Component
public class DocumentsValidator extends PropertiesValidator {

    public DocumentsValidator() {
        super(new Class[]{UploadDocumentsGroup.class, Default.class}, "otherSocialWorkDocuments",
            "socialWorkCarePlanDocument",
            "socialWorkStatementDocument",
            "socialWorkAssessmentDocument",
            "socialWorkChronologyDocument",
            "checklistDocument",
            "thresholdDocument",
            "socialWorkEvidenceTemplateDocument");
    }
}
