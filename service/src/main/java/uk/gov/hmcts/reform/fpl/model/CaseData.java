package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
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
import uk.gov.hmcts.reform.fpl.validation.groups.CaseExtensionGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.DateOfIssueGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.EPOGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingDetailsGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.NoticeOfProceedingsGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.SealedSDOGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.UploadDocumentsGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.ValidateFamilyManCaseNumberGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.epoordergroup.EPOEndDateGroup;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasDocumentsIncludedInSwet;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeDifference;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeNotMidnight;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeRange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@HasDocumentsIncludedInSwet(groups = UploadDocumentsGroup.class)
public class CaseData {
    @NotBlank(message = "Enter a case name")
    private final String caseName;
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
    private final List<@NotNull(message = "You need to add details to applicant") Element<Applicant>> applicants;

    @Valid
    @NotNull(message = "You need to add details to respondents")
    private final List<@NotNull(message = "You need to add details to respondents") Element<Respondent>> respondents1;

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
    private final List<Element<Direction>> localAuthorityDirections;
    private final List<Element<Direction>> localAuthorityDirectionsCustom;
    private final List<Element<Direction>> courtDirections;
    private final List<Element<Direction>> courtDirectionsCustom;
    private final List<Element<Direction>> cafcassDirections;
    private final List<Element<Direction>> cafcassDirectionsCustom;
    private final List<Element<Direction>> otherPartiesDirections;
    private final List<Element<Direction>> otherPartiesDirectionsCustom;
    private final List<Element<Direction>> respondentDirections;
    private final List<Element<Direction>> respondentDirectionsCustom;

    @JsonUnwrapped
    private Directions directionsForCaseManagementOrder;

    public Directions getDirectionsForCaseManagementOrder() {
        if (directionsForCaseManagementOrder != null && directionsForCaseManagementOrder.containsDirections()) {
            return directionsForCaseManagementOrder;
        }

        return null;
    }

    private final List<Element<Placement>> placements;
    private final Order standardDirectionOrder;

    @NotNull(message = "You need to enter the allocated judge.",
             groups = {SealedSDOGroup.class, HearingBookingDetailsGroup.class})
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
    @NotEmpty(message = "You need to enter a hearing date.", groups = SealedSDOGroup.class)
    private final List<Element<HearingBooking>> hearingDetails;

    private LocalDate dateSubmitted;
    private final List<Element<DocumentBundle>> noticeOfProceedingsBundle;
    private final List<Element<Recipients>> statementOfService;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final C2DocumentBundle temporaryC2Document;
    private final List<Element<C2DocumentBundle>> c2DocumentBundle;

    @JsonIgnore
    public C2DocumentBundle getLastC2DocumentBundle() {
        return Stream.of(ElementUtils.unwrapElements(c2DocumentBundle))
            .filter(list -> !list.isEmpty())
            .map(c2DocumentBundles -> c2DocumentBundles.get(c2DocumentBundles.size() - 1))
            .findFirst()
            .orElse(null);
    }

    private final Map<String, C2ApplicationType> c2ApplicationType;
    private final OrderTypeAndDocument orderTypeAndDocument;
    private final FurtherDirections orderFurtherDirections;
    private final GeneratedOrder order;
    private final Integer orderMonths;
    private final InterimEndDate interimEndDate;
    private final ChildSelector childSelector;
    private final String orderAppliesToAllChildren;
    @PastOrPresent(message = "Date of issue cannot be in the future", groups = DateOfIssueGroup.class)
    private final LocalDate dateOfIssue;
    private final List<Element<GeneratedOrder>> orderCollection;

    public List<Element<GeneratedOrder>> getOrderCollection() {
        return orderCollection != null ? orderCollection : new ArrayList<>();
    }

    @JsonIgnore
    private CaseManagementOrder caseManagementOrder;

    public CaseManagementOrder getCaseManagementOrder() {
        return prepareCaseManagementOrder();
    }

    private CaseManagementOrder prepareCaseManagementOrder() {
        //existing order
        Optional<CaseManagementOrder> oldOrder = ofNullable(caseManagementOrder);

        //hearing date list that cmo is heard in
        Optional<DynamicList> optionalDateList = ofNullable(cmoHearingDateList);
        UUID idFromDynamicList = optionalDateList.map(DynamicList::getValueCode).orElse(null);
        String hearingDate = optionalDateList.map(DynamicList::getValueLabel).orElse(null);

        //schedule
        Schedule scheduleFromOrder = oldOrder.map(CaseManagementOrder::getSchedule).orElse(null);

        //recital
        List<Element<Recital>> recitalsFromOrder = oldOrder.map(CaseManagementOrder::getRecitals).orElse(emptyList());

        //directions
        Optional<Directions> directions = ofNullable(getDirectionsForCaseManagementOrder());
        List<Element<Direction>> orderDirections = oldOrder.map(CaseManagementOrder::getDirections).orElse(emptyList());

        //date of issue
        Optional<LocalDate> optionalDateOfIssue = ofNullable(dateOfIssue);
        String stringDate = optionalDateOfIssue.map(date -> formatLocalDateToString(date, DATE)).orElse(null);

        CaseManagementOrder preparedOrder = CaseManagementOrder.builder()
            .id(oldOrder.map(CaseManagementOrder::getId).orElse(idFromDynamicList))
            .hearingDate(oldOrder.map(CaseManagementOrder::getHearingDate).orElse(hearingDate))
            .schedule(ofNullable(schedule).orElse(scheduleFromOrder))
            .recitals(ofNullable(recitals).orElse(recitalsFromOrder))
            .directions(directions.map(Directions::getDirectionsList).orElse(orderDirections))
            .dateOfIssue(oldOrder.map(CaseManagementOrder::getDateOfIssue).orElse(stringDate))
            .status(oldOrder.map(CaseManagementOrder::getStatus).orElse(null))
            .orderDoc(oldOrder.map(CaseManagementOrder::getOrderDoc).orElse(null))
            .action(oldOrder.map(CaseManagementOrder::getAction).orElse(null))
            .nextHearing(oldOrder.map(CaseManagementOrder::getNextHearing).orElse(null))
            .build();

        preparedOrder.setActionWithNullDocument(orderAction);

        if (preparedOrder.isSealed() && nextHearingDateList != null) {
            preparedOrder.setNextHearingFromDynamicElement(nextHearingDateList.getValue());
        }

        return preparedOrder;
    }

    @JsonGetter("caseManagementOrder")
    private CaseManagementOrder getCaseManagementOrderForLocalAuthority() {
        if (caseManagementOrder != null && caseManagementOrder.getStatus() != SEND_TO_JUDGE) {
            return caseManagementOrder;
        }
        return null;
    }

    @JsonSetter("caseManagementOrder")
    private void setCaseManagementOrderForLocalAuthority(CaseManagementOrder order) {
        if (order != null) {
            caseManagementOrder = order;
        }
    }

    @JsonGetter("cmoToAction")
    private CaseManagementOrder getCaseManagementOrderForJudiciary() {
        if (caseManagementOrder != null && caseManagementOrder.getStatus() == SEND_TO_JUDGE) {
            return caseManagementOrder;
        }
        return null;
    }

    @JsonSetter("cmoToAction")
    private void setCaseManagementOrderForJudiciary(CaseManagementOrder order) {
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
    public List<Element<Proceeding>> getAllProceedings() {
        List<Element<Proceeding>> proceedings = new ArrayList<>();

        ofNullable(this.getProceeding()).map(ElementUtils::element).ifPresent(proceedings::add);
        ofNullable(this.getProceeding())
            .map(Proceeding::getAdditionalProceedings).ifPresent(proceedings::addAll);

        return Collections.unmodifiableList(proceedings);
    }

    @JsonIgnore
    public String getRelevantProceedings() {
        return ofNullable(this.getProceeding())
            .map(Proceeding::getOnGoingProceeding)
            .orElse("");
    }

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

    private final String caseNote;
    private final List<Element<CaseNote>> caseNotes;
    private final List<Element<EmailAddress>> gatekeeperEmails;

    @JsonIgnore
    public String getComplianceDeadline() {
        return formatLocalDateToString(dateSubmitted.plusWeeks(26), FormatStyle.LONG);
    }

    private final String amountToPay;

    private LocalDate caseCompletionDate;
    @FutureOrPresent(message = "Enter an end date in the future", groups = CaseExtensionGroup.class)
    private LocalDate extensionDateOther;
    @FutureOrPresent(message = "Enter an end date in the future", groups = CaseExtensionGroup.class)
    private LocalDate eightWeeksExtensionDateOther;
    private final CaseExtensionTime caseExtensionTimeList;
    private final CaseExtensionTime caseExtensionTimeConfirmationList;
}
