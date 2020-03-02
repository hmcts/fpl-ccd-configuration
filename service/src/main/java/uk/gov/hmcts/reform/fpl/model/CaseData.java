package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOPhrase;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.validation.groups.EPOGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.NoticeOfProceedingsGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.UploadDocumentsGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.ValidateFamilyManCaseNumberGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.epoordergroup.EPOEndDateGroup;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasDocumentsIncludedInSwet;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeDifference;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeNotMidnight;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeRange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@HasDocumentsIncludedInSwet(groups = UploadDocumentsGroup.class)
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
    @NotNull(message = "You need to add details to respondents")
    private final List<@NotNull(message = "You need to add details to respondents") Element<Respondent>> respondents1;

    @Valid
    private Optional<Respondent> getFirstRespondent() {
        return findRespondent(0);
    }

    private final Proceeding proceeding;

    @NotNull(message = "You need to add details to solicitor")
    @Valid
    private final Solicitor solicitor;
    private final FactorsParenting factorsParenting;

    @NotNull(message = "You need to add details to allocation proposal")
    @Valid
    private final Allocation allocationProposal;

    private final Allocation allocationDecision;
    private final List<Element<Direction>> allParties;
    private final List<Element<Direction>> allPartiesCustom;
    private final List<Element<Direction>> allPartiesCustomCMO;
    private final List<Element<Direction>> localAuthorityDirections;
    private final List<Element<Direction>> localAuthorityDirectionsCustom;
    private final List<Element<Direction>> localAuthorityDirectionsCustomCMO;
    private final List<Element<Direction>> courtDirections;
    private final List<Element<Direction>> courtDirectionsCustom;
    private final List<Element<Direction>> courtDirectionsCustomCMO;
    private final List<Element<Direction>> cafcassDirections;
    private final List<Element<Direction>> cafcassDirectionsCustom;
    private final List<Element<Direction>> cafcassDirectionsCustomCMO;
    private final List<Element<Direction>> otherPartiesDirections;
    private final List<Element<Direction>> otherPartiesDirectionsCustom;
    private final List<Element<Direction>> otherPartiesDirectionsCustomCMO;
    private final List<Element<Direction>> respondentDirections;
    private final List<Element<Direction>> respondentDirectionsCustom;
    private final List<Element<Direction>> respondentDirectionsCustomCMO;
    private final List<Element<Placement>> placements;
    private final Order standardDirectionOrder;
    private final Judge allocatedJudge;
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
    @Valid
    private final Document socialWorkEvidenceTemplateDocument;
    @NotNull(message = "You need to add details to children")
    @Valid
    private final List<@NotNull(message = "You need to add details to children") Element<Child>> children1;
    @NotBlank(message = "Enter Familyman case number", groups = {NoticeOfProceedingsGroup.class,
        ValidateFamilyManCaseNumberGroup.class})
    private final String familyManCaseNumber;
    private final NoticeOfProceedings noticeOfProceedings;

    @JsonIgnore
    public List<Element<Applicant>> getAllApplicants() {
        return applicants != null ? applicants : new ArrayList<>();
    }

    @JsonIgnore
    public List<Element<Respondent>> getAllRespondents() {
        return respondents1 != null ? respondents1 : new ArrayList<>();
    }

    @JsonIgnore
    public List<Element<Child>> getAllChildren() {
        return children1 != null ? children1 : new ArrayList<>();
    }

    @NotNull(message = "Enter hearing details", groups = NoticeOfProceedingsGroup.class)
    private final List<Element<HearingBooking>> hearingDetails;

    private LocalDate dateSubmitted;
    private final List<Element<DocumentBundle>> noticeOfProceedingsBundle;
    private final List<Element<Recipients>> statementOfService;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final C2DocumentBundle temporaryC2Document;
    private final List<Element<C2DocumentBundle>> c2DocumentBundle;
    private final Map<String, C2ApplicationType> c2ApplicationType;
    private final FeesData feesData;
    private final OrderTypeAndDocument orderTypeAndDocument;
    private final FurtherDirections orderFurtherDirections;
    private final GeneratedOrder order;
    private final Integer orderMonths;
    private final InterimEndDate interimEndDate;
    private final ChildSelector childSelector;
    private final String orderAppliesToAllChildren;
    private final List<Element<GeneratedOrder>> orderCollection;

    public List<Element<GeneratedOrder>> getOrderCollection() {
        return orderCollection != null ? orderCollection : new ArrayList<>();
    }

    @JsonIgnore
    private CaseManagementOrder caseManagementOrder;

    @JsonGetter("caseManagementOrder")
    private CaseManagementOrder getCaseManagementOrder_LocalAuthority() {
        if (caseManagementOrder != null && caseManagementOrder.getStatus() != SEND_TO_JUDGE) {
            return caseManagementOrder;
        }
        return null;
    }

    @JsonSetter("caseManagementOrder")
    private void setCaseManagementOrder_LocalAuthority(CaseManagementOrder order) {
        if (order != null) {
            caseManagementOrder = order;
        }
    }

    @JsonGetter("cmoToAction")
    private CaseManagementOrder getCaseManagementOrder_Judiciary() {
        if (caseManagementOrder != null && caseManagementOrder.getStatus() == SEND_TO_JUDGE) {
            return caseManagementOrder;
        }
        return null;
    }

    @JsonSetter("cmoToAction")
    private void setCaseManagementOrder_Judiciary(CaseManagementOrder order) {
        if (order != null) {
            caseManagementOrder = order;
        }
    }

    private final OrderAction orderAction;
    private final DynamicList cmoHearingDateList;
    private final Schedule schedule;
    private final List<Element<Recital>> recitals;
    private final DocumentReference sharedDraftCMODocument;

    private final List<Element<CaseManagementOrder>> servedCaseManagementOrders;

    public List<Element<CaseManagementOrder>> getServedCaseManagementOrders() {
        return defaultIfNull(servedCaseManagementOrders, new ArrayList<>());
    }

    private final Others others;
    private final DynamicList nextHearingDateList;

    private final List<Element<Representative>> representatives;

    // EPO Order
    private final EPOChildren epoChildren;
    @TimeNotMidnight(message = "Enter a valid end time", groups = EPOEndDateGroup.class)
    @Future(message = "Enter an end date in the future", groups = EPOEndDateGroup.class)
    @TimeRange(message = "Date must be within the next 8 days", groups = EPOEndDateGroup.class,
        maxDate = @TimeDifference(amount = 8, unit = DAYS))
    private final LocalDateTime epoEndDate;
    private final EPOPhrase epoPhrase;
    private final EPOType epoType;
    @Valid
    private final Address epoRemovalAddress;

    @JsonIgnore
    public List<Element<Other>> getAllOthers() {
        List<Element<Other>> othersList = new ArrayList<>();

        ofNullable(this.getOthers()).map(Others::getFirstOther).map(ElementUtils::element).ifPresent(othersList::add);
        ofNullable(this.getOthers()).map(Others::getAdditionalOthers).ifPresent(othersList::addAll);

        return Collections.unmodifiableList(othersList);
    }

    public Optional<Other> findOther(int sequenceNo) {
        List<Other> allOthers = this.getAllOthers().stream().map(Element::getValue).collect(toList());

        return allOthers.size() <= sequenceNo ? empty() : Optional.of(allOthers.get(sequenceNo));
    }

    public Optional<Respondent> findRespondent(int seqNo) {
        return isEmpty(getRespondents1()) || getRespondents1().size() <= seqNo
            ? empty() : Optional.of(getRespondents1().get(seqNo).getValue());
    }

    public Optional<Applicant> findApplicant(int seqNo) {
        if (isEmpty(applicants) || applicants.size() <= seqNo) {
            return empty();
        } else {
            return Optional.of(applicants.get(seqNo).getValue());
        }
    }

    @JsonIgnore
    public String getFurtherDirectionsText() {
        return Optional.ofNullable(orderFurtherDirections).map(FurtherDirections::getDirections).orElse("");
    }

    private final List<Element<Child>> confidentialChildren;

    public List<Element<Child>> getConfidentialChildren() {
        return confidentialChildren != null ? confidentialChildren : new ArrayList<>();
    }

    private final List<Element<Respondent>> confidentialRespondents;

    public List<Element<Respondent>> getConfidentialRespondents() {
        return confidentialRespondents != null ? confidentialRespondents : new ArrayList<>();
    }

    private final List<Element<Other>> confidentialOthers;

    public List<Element<Other>> getConfidentialOthers() {
        return Optional.ofNullable(confidentialOthers).orElse(new ArrayList<>());
    }

    @JsonGetter("confidentialPlacements")
    public List<Element<Placement>> getPlacements() {
        return defaultIfNull(placements, new ArrayList<>());
    }
}
