package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.validators.interfaces.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@HasEPOGrounds
@DocumentsSequenceGroup
@EPOSequenceGroup
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
    private final List<Element<DocumentSocialWorkOther>> documents_socialWorkOther;
    private final Document documents_socialWorkCarePlan_document;
    private final Document documents_socialWorkStatement_document;
    private final Document documents_socialWorkAssessment_document;
    private final Document documents_socialWorkChronology_document;
    private final Document documents_checklist_document;
    private final Document documents_threshold_document;
    private final Document documents_socialWorkEvidenceTemplate_document;
}
