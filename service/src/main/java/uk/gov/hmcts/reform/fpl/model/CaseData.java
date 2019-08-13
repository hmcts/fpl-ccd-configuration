package uk.gov.hmcts.reform.fpl.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasDocumentStatus;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasEPOGrounds;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasThresholdCriteria;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasThresholdCriteriaDetails;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@HasEPOGrounds
@HasThresholdCriteria
@HasThresholdCriteriaDetails
@HasDocumentStatus

public class CaseData {

    @NotBlank(message = "Enter a case name")
    private final String caseName;
    private final String gatekeeperEmail;
    private final String caseLocalAuthority;
    private final Risks risks;
    @NotNull(message = "Select at least one type of order")
    @Valid
    private final Orders orders;
    private final Grounds grounds;
    private final GroundsForEPO groundsForEPO;
    @NotNull(message = "You need to add details to children")
    @Valid
    private final Children children;
    @NotNull(message = "You need to add details to applicant")
    @Valid
    private final Applicant applicant;
    private final List<Element<Respondent>> respondent;
    private final List<Element<RespondentParty>> respondent1;
    private final Proceeding proceeding;
    private final Solicitor solicitor;
    private final FactorsParenting factorsParenting;
    private final AllocationProposal allocationProposal;
    @NotNull(message = "You need to add details to hearing")
    @Valid
    private final Hearing hearing;
    private final HearingPreferences hearingPreferences;
    private final InternationalElement internationalElement;

    @Getter(AccessLevel.NONE)
    private final List<Element<DocumentSocialWorkOther>> documents_socialWorkOther;
    public List<Element<DocumentSocialWorkOther>> socialWorkOther() {
        return documents_socialWorkOther;
    }

    @Getter(AccessLevel.NONE)
    private final Document documents_socialWorkCarePlan_document;
    public Document socialWorkCarePlan() {
        return documents_socialWorkCarePlan_document;
    }

    @Getter(AccessLevel.NONE)
    private final Document documents_socialWorkStatement_document;
    public Document socialWorkStatement() {
        return documents_socialWorkStatement_document;
    }

    @Getter(AccessLevel.NONE)
    private final Document documents_socialWorkAssessment_document;
    public Document socialWorkAssessment() {
        return documents_socialWorkAssessment_document;
    }

    @Getter(AccessLevel.NONE)
    private final Document documents_socialWorkChronology_document;
    public Document socialWorkChronology() {
        return documents_socialWorkChronology_document;
    }

    @Getter(AccessLevel.NONE)
    private final Document documents_checklist_document;
    public Document checklist() {
        return documents_checklist_document;
    }

    @Getter(AccessLevel.NONE)
    private final Document documents_threshold_document;
    public Document threshold() {
        return documents_threshold_document;
    }

    @Getter(AccessLevel.NONE)
    private final Document documents_socialWorkEvidenceTemplate_document;
    public Document socialWorkEvidence() {
        return documents_socialWorkEvidenceTemplate_document;
    }
}
