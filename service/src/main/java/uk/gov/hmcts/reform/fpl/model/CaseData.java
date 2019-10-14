package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ProceedingType;
import uk.gov.hmcts.reform.fpl.interfaces.NoticeOfProceedingsGroup;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.validators.interfaces.EPOGroup;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
public class CaseData {
    @NotBlank(message = "Enter a case name")
    private final String caseName;
    private final String gatekeeperEmail;
    private final String caseLocalAuthority;
    private final Risks risks;
    @NotNull(message = "You need to add details to orders and directions needed")
    @Valid
    private final Orders orders;
    @NotNull(message = "You need to add details to grounds for the application")
    @Valid
    private final Grounds grounds;
    @NotNull(message = "You need to add details to grounds for the application", groups = EPOGroup.class)
    @Valid
    private final GroundsForEPO groundsForEPO;
    @NotNull(message = "You need to add details to applicant")
    @Valid
    private final List<@NotNull(message = "You need to add details to applicant")
        Element<Applicant>> applicants;
    private final List<Element<RespondentParty>> respondents1;
    private final Proceeding proceeding;
    private final Solicitor solicitor;
    private final FactorsParenting factorsParenting;
    private final Allocation allocationProposal;
    private final Allocation allocationDecision;
    @NotNull(message = "You need to add details to hearing needed")
    @Valid
    private final Hearing hearing;
    private final HearingPreferences hearingPreferences;
    private final InternationalElement internationalElement;
    @JsonProperty("documents_socialWorkOther")
    private final List<Element<DocumentSocialWorkOther>> otherSocialWorkDocuments;
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
    @NotNull(message = "You need to add details to children")
    @Valid
    private final List<@NotNull(message = "You need to add details to children")Element<Child>> children1;
    @NotBlank(message = "Enter Familyman case number", groups = NoticeOfProceedingsGroup.class)
    private final String familyManCaseNumber;
    private List<ProceedingType> proceedingTypes;

    public List<Element<Applicant>> getAllApplicants() {
        return applicants != null ? applicants : new ArrayList<>();
    }

    public List<Element<Child>> getAllChildren() {
        return children1 != null ? children1 : new ArrayList<>();
    }

    @NotNull(message = "Enter hearing details", groups = NoticeOfProceedingsGroup.class)
    private final List<Element<HearingBooking>> hearingDetails;
    private final List<Element<DocumentBundle>> noticeOfProceedingsBundle;
    private final List<Element<Recipients>> statementOfService;
}
