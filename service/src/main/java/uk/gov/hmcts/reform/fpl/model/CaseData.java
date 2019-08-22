package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasEPOGrounds;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasThresholdDetails;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasThresholdReason;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@HasEPOGrounds
@HasThresholdReason
@HasThresholdDetails
@SuppressWarnings("membername")
public class CaseData {
    @NotBlank(message = "Enter a case name")
    private final String caseName;
    private final String gatekeeperEmail;
    private final String caseLocalAuthority;
    private final Risks risks;
    @NotNull(message = "You need to add details to orders and directions needed")
    @Valid
    private final Orders orders;
    private final Grounds grounds;
    private final GroundsForEPO groundsForEPO;
    @NotNull(message = "You need to add details to children")
    @Valid
    private final Children children;
    @NotNull(message = "You need to add details to applicant")
    private final List<Element<Applicant>> applicants;

    @Valid
    public final Applicant getMainApplicant() {
        if (applicants != null && applicants.get(0) != null && applicants.get(0).getValue() != null) {
            return applicants.get(0).getValue();
        } else {
            return Applicant.builder().build();
        }
    }

    private final List<Element<RespondentParty>> respondent;
    private final Proceeding proceeding;
    private final Solicitor solicitor;
    private final FactorsParenting factorsParenting;
    private final AllocationProposal allocationProposal;
    @NotNull(message = "You need to add details to hearing needed")
    @Valid
    private final Hearing hearing;
    private final HearingPreferences hearingPreferences;
    private final InternationalElement internationalElement;

    @JsonProperty("documents_socialWorkOther")
    private final List<Element<DocumentSocialWorkOther>> socialWorkOtherDocuments;

    @JsonProperty("documents_socialWorkCarePlan_document")
    @NotNull(message = "Tell us the status of all documents including those that you haven't uploaded")
    @Valid
    public final Document socialWorkCarePlanDocument;

    @JsonProperty("documents_socialWorkStatement_document")
    @NotNull(message = "Tell us the status of all documents including those that you haven't uploaded")
    @Valid
    public final Document socialWorkStatementDocument;

    @JsonProperty("documents_socialWorkAssessment_document")
    @NotNull(message = "Tell us the status of all documents including those that you haven't uploaded")
    @Valid
    public final Document socialWorkAssessmentDocument;

    @JsonProperty("documents_socialWorkChronology_document")
    @NotNull(message = "Tell us the status of all documents including those that you haven't uploaded")
    @Valid
    public final Document socialWorkChronologyDocument;

    @JsonProperty("documents_checklist_document")
    @NotNull(message = "Tell us the status of all documents including those that you haven't uploaded")
    @Valid
    public final Document checklistDocument;

    @JsonProperty("documents_threshold_document")
    @NotNull(message = "Tell us the status of all documents including those that you haven't uploaded")
    @Valid
    public final Document thresholdDocument;

    @JsonProperty("documents_socialWorkEvidenceTemplate_document")
    private final Document socialWorkEvidenceTemplateDocument;
}
