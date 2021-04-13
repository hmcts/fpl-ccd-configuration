package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime;
import uk.gov.hmcts.reform.fpl.enums.EPOExclusionRequirementType;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.HearingReListOption;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentSubtypeListLA;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.OutsourcingType;
import uk.gov.hmcts.reform.fpl.enums.ProceedingType;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SDORoute;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingPresence;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOPhrase;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.interfaces.ApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.generated.OrderExclusionClause;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.IncrementalInteger;
import uk.gov.hmcts.reform.fpl.validation.groups.CaseExtensionGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.DateOfIssueGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.EPOGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingDetailsGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingDatesGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingEndDateGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.MigrateStateGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.NoticeOfProceedingsGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.SealedSDOGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.UploadDocumentsGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.ValidateFamilyManCaseNumberGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.epoordergroup.EPOEndDateGroup;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasDocumentsIncludedInSwet;
import uk.gov.hmcts.reform.fpl.validation.interfaces.IsStateMigratable;
import uk.gov.hmcts.reform.fpl.validation.interfaces.IsValidHearingEdit;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.EPOTimeRange;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasHearingEndDateAfterStartDate;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeDifference;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeNotMidnight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@HasDocumentsIncludedInSwet(groups = UploadDocumentsGroup.class)
@IsStateMigratable(groups = MigrateStateGroup.class)
@IsValidHearingEdit(groups = HearingBookingGroup.class)
@HasHearingEndDateAfterStartDate(message = "The end date and time must be after the start date and time",
    groups = HearingEndDateGroup.class)
@EPOTimeRange(message = "Date must be within 8 days of the order date", groups = EPOEndDateGroup.class,
    maxDate = @TimeDifference(amount = 8, unit = DAYS))
public class CaseData {
    private final Long id;
    private final State state;
    @NotBlank(message = "Enter a case name")
    private final String caseName;
    private final String caseLocalAuthority;
    private final String caseLocalAuthorityName;
    private OrganisationPolicy localAuthorityPolicy;
    private OrganisationPolicy outsourcingPolicy;
    private OutsourcingType outsourcingType;
    private Object outsourcingLAs;

    private final Risks risks;
    @NotNull(message = "Add the orders and directions sought")
    @Valid
    private final Orders orders;
    @NotNull(message = "Add the grounds for the application")
    @Valid
    private final Grounds grounds;
    @NotNull(message = "Add the grounds for the application", groups = EPOGroup.class)
    @Valid
    private final GroundsForEPO groundsForEPO;
    @NotEmpty(message = "Add applicant's details")
    @Valid
    private final List<@NotNull(message = "Add applicant's details") Element<Applicant>> applicants;

    @Valid
    @NotEmpty(message = "Add the respondents' details")
    private final List<@NotNull(message = "Add the respondents' details") Element<Respondent>> respondents1;

    public DynamicList buildRespondentStatementDynamicList(UUID selected) {
        return asDynamicList(getAllRespondents(), selected,
            respondent -> respondent.getParty().getFullName());
    }

    public DynamicList buildRespondentStatementDynamicList() {
        return buildRespondentStatementDynamicList(null);
    }

    private final Proceeding proceeding;

    @NotNull(message = "Add the applicant's solicitor's details")
    @Valid
    private final Solicitor solicitor;
    private final FactorsParenting factorsParenting;

    @NotNull(message = "Add the allocation proposal")
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

    private final List<Element<Placement>> placements;
    private final StandardDirectionOrder standardDirectionOrder;
    private final List<Element<StandardDirectionOrder>> hiddenStandardDirectionOrders;

    public List<Element<StandardDirectionOrder>> getHiddenStandardDirectionOrders() {
        return defaultIfNull(hiddenStandardDirectionOrders, new ArrayList<>());
    }

    private SDORoute sdoRouter;
    private final DocumentReference preparedSDO;
    private final DocumentReference replacementSDO;

    @NotNull(message = "You need to enter the allocated judge.",
        groups = {SealedSDOGroup.class, HearingBookingDetailsGroup.class})
    private final Judge allocatedJudge;
    @NotNull(message = "Add the hearing urgency details")
    @Valid
    private final Hearing hearing;
    private final HearingPreferences hearingPreferences;
    private final InternationalElement internationalElement;

    @JsonProperty("documents_socialWorkOther")
    private final List<Element<DocumentSocialWorkOther>> otherSocialWorkDocuments;

    @JsonProperty("documents_socialWorkCarePlan_document")
    @NotNull(message = "Add social work documents, or details of when you'll send them")
    @Valid
    public final Document socialWorkCarePlanDocument;
    @JsonProperty("documents_socialWorkStatement_document")
    @NotNull(message = "Add social work documents, or details of when you'll send them")
    @Valid
    public final Document socialWorkStatementDocument;
    @JsonProperty("documents_socialWorkAssessment_document")
    @NotNull(message = "Add social work documents, or details of when you'll send them")
    @Valid
    public final Document socialWorkAssessmentDocument;
    @JsonProperty("documents_socialWorkChronology_document")
    @NotNull(message = "Add social work documents, or details of when you'll send them")
    @Valid
    public final Document socialWorkChronologyDocument;
    @JsonProperty("documents_checklist_document")
    @NotNull(message = "Add social work documents, or details of when you'll send them")
    @Valid
    public final Document checklistDocument;
    @JsonProperty("documents_threshold_document")
    @NotNull(message = "Add social work documents, or details of when you'll send them")
    @Valid
    public final Document thresholdDocument;
    @JsonProperty("documents_socialWorkEvidenceTemplate_document")
    @Valid
    public final Document socialWorkEvidenceTemplateDocument;
    public final CourtBundle courtBundle;
    @NotEmpty(message = "Add the child's details")
    @Valid
    private final List<@NotNull(message = "Add the child's details") Element<Child>> children1;
    @NotBlank(message = "Enter Familyman case number", groups = {NoticeOfProceedingsGroup.class,
        ValidateFamilyManCaseNumberGroup.class})
    private final String familyManCaseNumber;
    private final NoticeOfProceedings noticeOfProceedings;
    private final List<Element<SentDocuments>> documentsSentToParties;

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

    //TODO add null-checker getter for hearingDetails during refactor/removal of legacy code (FPLA-2280)
    @NotNull(message = "Enter hearing details", groups = NoticeOfProceedingsGroup.class)
    @NotEmpty(message = "You need to enter a hearing date.", groups = SealedSDOGroup.class)
    @JsonProperty
    private List<Element<HearingBooking>> hearingDetails;
    @JsonProperty
    private List<Element<HearingBooking>> cancelledHearingDetails;
    private final List<Element<UUID>> selectedHearingIds;

    @JsonIgnore
    public Optional<Element<HearingBooking>> findHearingBookingElement(UUID elementId) {
        return findElement(elementId, hearingDetails);
    }

    @JsonIgnore
    public Optional<Element<HearingBooking>> getHearingLinkedToCMO(UUID removedOrderId) {
        return hearingDetails.stream()
            .filter(hearingBookingElement ->
                removedOrderId.equals(hearingBookingElement.getValue().getCaseManagementOrderId()))
            .findFirst();
    }

    private LocalDate dateSubmitted;
    private final List<Element<DocumentBundle>> noticeOfProceedingsBundle;
    private final List<Element<Recipients>> statementOfService;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final C2DocumentBundle temporaryC2Document;
    private final OtherApplicationsBundle temporaryOtherApplicationsBundle;
    private final PBAPayment temporaryPbaPayment;
    private final List<Element<C2DocumentBundle>> c2DocumentBundle;
    private final List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle;

    @JsonIgnore
    public boolean hasC2DocumentBundle() {
        return isNotEmpty(c2DocumentBundle);
    }

    @JsonIgnore
    public boolean hasApplicationBundles() {
        return isNotEmpty(c2DocumentBundle) || isNotEmpty(additionalApplicationsBundle);
    }

    @JsonIgnore
    public C2DocumentBundle getLastC2DocumentBundle() {
        return Stream.of(ElementUtils.unwrapElements(c2DocumentBundle))
            .filter(list -> !list.isEmpty())
            .map(c2DocumentBundles -> c2DocumentBundles.get(c2DocumentBundles.size() - 1))
            .findFirst()
            .orElse(null);
    }

    @JsonIgnore
    public C2DocumentBundle getC2DocumentBundleByUUID(UUID elementId) {
        return nullSafeList(c2DocumentBundle).stream()
            .filter(c2DocumentBundleElement -> c2DocumentBundleElement.getId().equals(elementId))
            .map(Element::getValue)
            .findFirst()
            .orElse(null);
    }

    public DynamicList buildC2DocumentDynamicList() {
        return buildC2DocumentDynamicList(null);
    }

    public DynamicList buildC2DocumentDynamicList(UUID selected) {
        IncrementalInteger i = new IncrementalInteger(1);
        return asDynamicList(c2DocumentBundle, selected, documentBundle -> documentBundle.toLabel(i.getAndIncrement()));
    }

    public DynamicList buildApplicationBundlesDynamicList() {
        return buildApplicationBundlesDynamicList(null);
    }

    public DynamicList buildApplicationBundlesDynamicList(UUID selected) {
        List<Element<ApplicationsBundle>> applicationsBundles = getAllApplicationsBundles();
        applicationsBundles
            .sort(Comparator.comparing(
                (Element<ApplicationsBundle> bundle) -> bundle.getValue().getSortOrder())
                .thenComparing((Element<ApplicationsBundle> bundle) -> bundle.getValue().toLabel()));

        return asDynamicList(applicationsBundles, selected, ApplicationsBundle::toLabel);
    }

    @JsonIgnore
    public List<Element<ApplicationsBundle>> getAllApplicationsBundles() {
        List<Element<ApplicationsBundle>> applicationBundles = new ArrayList<>();

        ofNullable(c2DocumentBundle).ifPresent(
            bundle -> bundle.forEach(c2 -> applicationBundles.add(element(c2.getId(), c2.getValue()))));

        unwrapElements(getAdditionalApplicationsBundle()).forEach(
            bundle -> {
                ofNullable(bundle.getC2DocumentBundle()).ifPresent(
                    c2 -> applicationBundles.add(element(c2.getId(), c2)));

                ofNullable(bundle.getOtherApplicationsBundle()).ifPresent(
                    otherBundle -> applicationBundles.add(element(otherBundle.getId(), otherBundle)));
            }
        );

        return applicationBundles;
    }

    @JsonIgnore
    public ApplicationsBundle getApplicationBundleByUUID(UUID elementId) {
        return getAllApplicationsBundles().stream()
            .filter(bundleElement -> bundleElement.getId().equals(elementId))
            .map(Element::getValue)
            .findFirst()
            .orElse(null);
    }

    private final Map<String, C2ApplicationType> c2ApplicationType;
    private final C2ApplicationType c2Type;
    private final OrderTypeAndDocument orderTypeAndDocument;
    private final List<AdditionalApplicationType> additionalApplicationType;

    public List<AdditionalApplicationType> getAdditionalApplicationType() {
        return defaultIfNull(additionalApplicationType, emptyList());
    }

    private final FurtherDirections orderFurtherDirections;
    private final OrderExclusionClause orderExclusionClause;
    private final GeneratedOrder order;
    private final DocumentReference uploadedOrder;
    @JsonIgnore
    private OrderStatus generatedOrderStatus;
    private final Integer orderMonths;
    private final InterimEndDate interimEndDate;
    private final Selector childSelector;
    private final Selector careOrderSelector;
    private final Selector newHearingSelector;

    private final String orderAppliesToAllChildren;

    public String getOrderAppliesToAllChildren() {
        return getAllChildren().size() == 1 ? YES.getValue() : orderAppliesToAllChildren;
    }

    private String remainingChildIndex;

    @PastOrPresent(message = "Date of issue cannot be in the future", groups = DateOfIssueGroup.class)
    private final LocalDate dateOfIssue;
    private final List<Element<GeneratedOrder>> orderCollection;

    public List<Element<GeneratedOrder>> getOrderCollection() {
        return orderCollection != null ? orderCollection : new ArrayList<>();
    }

    private final Object removableOrderList;
    private final String reasonToRemoveOrder;
    private final List<Element<GeneratedOrder>> hiddenOrders;

    public List<Element<GeneratedOrder>> getHiddenOrders() {
        return defaultIfNull(hiddenOrders, new ArrayList<>());
    }

    private final Others others;

    private final List<Element<Representative>> representatives;

    @JsonIgnore
    public List<Representative> getRepresentativesByServedPreference(RepresentativeServingPreferences preference) {
        if (isNotEmpty(representatives)) {
            return representatives.stream()
                .filter(Objects::nonNull)
                .map(Element::getValue)
                .filter(representative -> preference == representative.getServingPreferences())
                .collect(toList());
        }
        return emptyList();
    }

    private final List<Element<LegalRepresentative>> legalRepresentatives;

    // EPO Order
    @PastOrPresent(message = "Date of issue cannot be in the future", groups = DateOfIssueGroup.class)
    private final LocalDateTime dateAndTimeOfIssue;
    private final EPOChildren epoChildren;
    @TimeNotMidnight(message = "Enter a valid end time", groups = EPOEndDateGroup.class)
    @Future(message = "Enter an end date in the future", groups = EPOEndDateGroup.class)
    private final LocalDateTime epoEndDate;
    private final EPOPhrase epoPhrase;
    private final EPOType epoType;
    @Valid
    private final Address epoRemovalAddress;
    private final String epoWhoIsExcluded;
    private final LocalDate epoExclusionStartDate;
    private final EPOExclusionRequirementType epoExclusionRequirementType;

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

    public Optional<Element<Respondent>> findRespondent(UUID id) {
        return findElement(id, getAllRespondents());
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

    @JsonIgnore
    public String getExclusionClauseText() {
        return Optional.ofNullable(orderExclusionClause).map(OrderExclusionClause::getExclusionClause).orElse("");
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

    private final CloseCase closeCase;
    private final String deprivationOfLiberty;
    private final CloseCase closeCaseTabField;
    private final String closeCaseFromOrder;

    private final ManageDocument manageDocument;
    private final ManageDocumentLA manageDocumentLA;
    private final ManageDocumentSubtypeListLA manageDocumentSubtypeListLA;
    private final String manageDocumentsRelatedToHearing;
    private final List<Element<SupportingEvidenceBundle>> supportingEvidenceDocumentsTemp;
    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments; //general evidence
    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA; //general evidence
    private final List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceDocuments;
    private final List<Element<SupportingEvidenceBundle>> correspondenceDocuments;
    private final List<Element<SupportingEvidenceBundle>> correspondenceDocumentsLA;
    private final List<Element<SupportingEvidenceBundle>> c2SupportingDocuments;

    private final List<Element<RespondentStatement>> respondentStatements;
    private final Object manageDocumentsHearingList;
    private final Object manageDocumentsSupportingC2List;
    private final Object courtBundleHearingList;
    private final Object respondentStatementList;

    private final CourtBundle manageDocumentsCourtBundle;
    private final List<Element<CourtBundle>> courtBundleList;

    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceDocumentsTemp() {
        return defaultIfNull(supportingEvidenceDocumentsTemp, new ArrayList<>());
    }

    public List<Element<CourtBundle>> getCourtBundleList() {
        return defaultIfNull(courtBundleList, new ArrayList<>());
    }

    public List<Element<SupportingEvidenceBundle>> getCorrespondenceDocuments() {
        return defaultIfNull(correspondenceDocuments, new ArrayList<>());
    }

    public List<Element<HearingFurtherEvidenceBundle>> getHearingFurtherEvidenceDocuments() {
        return defaultIfNull(hearingFurtherEvidenceDocuments, new ArrayList<>());
    }

    public List<Element<RespondentStatement>> getRespondentStatements() {
        return defaultIfNull(respondentStatements, new ArrayList<>());
    }

    public Optional<Element<RespondentStatement>> getRespondentStatementByRespondentId(UUID id) {
        return getRespondentStatements().stream()
            .filter(respondentStatement -> respondentStatement.getValue().getRespondentId().equals(id))
            .findAny();
    }

    public boolean documentBundleContainsHearingId(UUID hearingId) {
        return getHearingFurtherEvidenceDocuments().stream()
            .anyMatch(element -> element.getId().equals(hearingId));
    }

    @JsonIgnore
    public boolean isClosedFromOrder() {
        return YES.getValue().equals(closeCaseFromOrder);
    }

    private final ReturnApplication returnApplication;

    public boolean allocatedJudgeExists() {
        return allocatedJudge != null;
    }

    public boolean hasAllocatedJudgeEmail() {
        return allocatedJudgeExists() && isNotEmpty(allocatedJudge.getJudgeEmailAddress());
    }

    @JsonIgnore
    public Optional<HearingBooking> getFirstHearing() {
        return unwrapElements(hearingDetails).stream()
            .min(comparing(HearingBooking::getStartDate));
    }

    @JsonIgnore
    public Optional<HearingBooking> getFirstHearingOfType(HearingType type) {
        return unwrapElements(hearingDetails).stream()
            .filter(hearingBooking -> hearingBooking.isOfType(type))
            .min(comparing(HearingBooking::getStartDate));
    }

    @JsonIgnore
    public Optional<HearingBooking> getNextHearingAfter(LocalDateTime time) {
        return unwrapElements(hearingDetails).stream()
            .filter(hearingBooking -> hearingBooking.getStartDate().isAfter(time))
            .min(comparing(HearingBooking::getStartDate));
    }

    @JsonIgnore
    public HearingBooking getMostUrgentHearingBookingAfter(LocalDateTime time) {
        return getNextHearingAfter(time).orElseThrow(NoHearingBookingException::new);
    }

    @JsonIgnore
    public List<Element<HearingBooking>> addCancelledHearingBooking(Element<HearingBooking> hearing) {
        if (cancelledHearingDetails == null) {
            cancelledHearingDetails = new ArrayList<>();
        }
        this.cancelledHearingDetails.add(hearing);
        return this.cancelledHearingDetails;
    }

    @JsonIgnore
    public List<Element<HearingBooking>> addHearingBooking(Element<HearingBooking> hearing) {
        if (hearingDetails == null) {
            hearingDetails = new ArrayList<>();
        }
        hearingDetails.add(hearing);
        return hearingDetails;
    }

    @JsonIgnore
    public List<Element<HearingBooking>> setHearingDetails(List<Element<HearingBooking>> hearings) {
        this.hearingDetails = hearings;
        return hearingDetails;
    }

    @JsonIgnore
    public void removeHearingDetails(Element<HearingBooking> hearing) {
        if (hearingDetails != null) {
            hearingDetails.remove(hearing);
        }
    }

    public boolean hasFutureHearing(List<Element<HearingBooking>> hearingBookings) {
        return isNotEmpty(hearingBookings) && hearingBookings.stream()
            .anyMatch(hearingBooking -> hearingBooking.getValue().startsAfterToday());
    }

    private final DocumentReference submittedForm;

    private final List<Element<HearingOrder>> draftUploadedCMOs;
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts;
    private final UUID lastHearingOrderDraftsHearingId;

    @JsonIgnore
    public List<Element<HearingOrdersBundle>> getBundlesForApproval() {
        return defaultIfNull(getHearingOrdersBundlesDrafts(), new ArrayList<Element<HearingOrdersBundle>>())
            .stream().filter(bundle -> isNotEmpty(bundle.getValue().getOrders(SEND_TO_JUDGE)))
            .collect(toList());
    }

    @JsonUnwrapped
    @Builder.Default
    private final UploadDraftOrdersData uploadDraftOrdersEventData = UploadDraftOrdersData.builder().build();

    public List<Element<HearingOrder>> getDraftUploadedCMOs() {
        return defaultIfNull(draftUploadedCMOs, new ArrayList<>());
    }

    public Optional<Element<HearingOrder>> getDraftUploadedCMOWithId(UUID orderId) {
        return getDraftUploadedCMOs().stream()
            .filter(draftCmoElement -> draftCmoElement.getId().equals(orderId))
            .findFirst();
    }

    @JsonIgnore
    public List<Element<HearingOrder>> getOrdersFromHearingOrderDraftsBundles() {
        if (hearingOrdersBundlesDrafts != null) {
            return hearingOrdersBundlesDrafts.stream()
                .map(Element::getValue)
                .flatMap((HearingOrdersBundle hearingOrdersBundle)
                    -> hearingOrdersBundle.getOrders().stream())
                .collect(toList());
        }

        return new ArrayList<>();
    }

    public Optional<Element<HearingOrdersBundle>> getHearingOrderBundleThatContainsOrder(UUID orderId) {
        return nullSafeList(hearingOrdersBundlesDrafts).stream()
            .filter(hearingOrdersBundleElement
                -> hearingOrdersBundleElement.getValue().getOrders().stream()
                .anyMatch(orderElement -> orderElement.getId().equals(orderId)))
            .findFirst();
    }

    @JsonIgnore
    public List<Element<HearingBooking>> getAllHearings() {
        return Stream.of(defaultIfNull(hearingDetails, new ArrayList<Element<HearingBooking>>()),
            defaultIfNull(cancelledHearingDetails, new ArrayList<Element<HearingBooking>>()))
            .flatMap(Collection::stream).collect(toList());
    }

    @JsonIgnore
    public List<Element<HearingBooking>> getPastHearings() {
        return defaultIfNull(hearingDetails, new ArrayList<Element<HearingBooking>>()).stream()
            .filter(hearingBooking -> !hearingBooking.getValue().startsAfterToday())
            .collect(toList());
    }

    @JsonIgnore
    public List<Element<HearingBooking>> getPastAndTodayHearings() {
        return defaultIfNull(hearingDetails, new ArrayList<Element<HearingBooking>>()).stream()
            .filter(hearingBooking -> hearingBooking.getValue().startsTodayOrBefore())
            .collect(toList());
    }

    @JsonIgnore
    public List<Element<HearingBooking>> getFutureHearings() {
        return defaultIfNull(hearingDetails, new ArrayList<Element<HearingBooking>>()).stream()
            .filter(hearingBooking -> hearingBooking.getValue().startsAfterToday())
            .collect(toList());
    }

    @JsonIgnore
    public List<Element<HearingBooking>> getFutureAndTodayHearings() {
        return defaultIfNull(hearingDetails, new ArrayList<Element<HearingBooking>>()).stream()
            .filter(hearingBooking -> hearingBooking.getValue().startsTodayOrAfter())
            .collect(toList());
    }

    @JsonIgnore
    public List<Element<HearingBooking>> getToBeReListedHearings() {
        return defaultIfNull(cancelledHearingDetails, new ArrayList<Element<HearingBooking>>()).stream()
            .filter(hearingBooking -> hearingBooking.getValue().isToBeReListed())
            .collect(toList());
    }

    private final Object cmoToReviewList;
    private final ReviewDecision reviewCMODecision;
    private final String numDraftCMOs;
    private final List<Element<HearingOrder>> sealedCMOs;
    private final List<Element<HearingOrder>> ordersToBeSent;

    @JsonUnwrapped
    private final ReviewDraftOrdersData reviewDraftOrdersData;

    public List<Element<HearingOrder>> getSealedCMOs() {
        return defaultIfNull(sealedCMOs, new ArrayList<>());
    }

    @JsonIgnore
    public Optional<HearingBooking> getNextHearingAfterCmo(UUID cmoID) {
        LocalDateTime currentCmoStartDate = unwrapElements(getAllHearings()).stream()
            .filter(hearingBooking -> cmoID.equals(hearingBooking.getCaseManagementOrderId()))
            .map(HearingBooking::getStartDate)
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("Failed to find hearing matching cmo id " + cmoID));

        return unwrapElements(hearingDetails).stream()
            .filter(hearingBooking -> hearingBooking.getStartDate().isAfter(currentCmoStartDate))
            .min(comparing(HearingBooking::getStartDate));
    }

    private final List<Element<HearingOrder>> hiddenCaseManagementOrders;

    @JsonIgnore
    public List<Element<HearingOrder>> getHiddenCMOs() {
        return defaultIfNull(hiddenCaseManagementOrders, new ArrayList<>());
    }

    private String sendToCtsc;
    private String displayAmountToPay;
    private final String confirmChangeState;

    public DynamicList buildDynamicHearingList() {
        return buildDynamicHearingList(null);
    }

    public DynamicList buildDynamicHearingList(UUID selected) {
        return asDynamicList(getHearingDetails(), selected, HearingBooking::toLabel);
    }

    private final HearingType hearingType;
    private final String hearingTypeDetails;
    private final String hearingVenue;
    private final Address hearingVenueCustom;
    private final String firstHearingFlag; //also used for logic surrounding legacy hearings
    private final PreviousHearingVenue previousHearingVenue;
    private String previousVenueId;
    private final String noticeOfHearingNotes;
    private final Object hearingDateList;
    private final Object pastAndTodayHearingDateList;
    private final Object futureAndTodayHearingDateList;
    private final Object toReListHearingDateList;
    private final String hasExistingHearings;
    private final UUID selectedHearingId;
    private final HearingPresence hearingPresence;

    @TimeNotMidnight(message = "Enter a valid start time", groups = HearingDatesGroup.class)
    @Future(message = "Enter a start date in the future", groups = HearingDatesGroup.class)
    private final LocalDateTime hearingStartDate;

    @TimeNotMidnight(message = "Enter a valid end time", groups = HearingDatesGroup.class)
    @Future(message = "Enter an end date in the future", groups = HearingDatesGroup.class)
    private final LocalDateTime hearingEndDate;
    private final String sendNoticeOfHearing;
    private final HearingOptions hearingOption;
    private final HearingReListOption hearingReListOption;
    private final HearingCancellationReason adjournmentReason;
    private final HearingCancellationReason vacatedReason;
    private final List<ProceedingType> proceedingType;
    private final State closedStateRadioList;

    private final LocalDateTime hearingEndDateConfirmation;
    private final LocalDateTime hearingStartDateConfirmation;

    @JsonIgnore
    public boolean isHearingDateInPast() {
        return hearingEndDate.isBefore(LocalDateTime.now()) || hearingStartDate.isBefore(LocalDateTime.now());
    }

    private final List<Element<ApplicationDocument>> applicationDocuments;
    private final String applicationDocumentsToFollowReason;

    @JsonUnwrapped
    @Builder.Default
    private final MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder().build();
    private final List<Element<JudicialMessage>> judicialMessages;
    private final List<Element<JudicialMessage>> closedJudicialMessages;

    public DynamicList buildJudicialMessageDynamicList(UUID selected) {
        return asDynamicList(judicialMessages, selected, JudicialMessage::toLabel);
    }

    public DynamicList buildJudicialMessageDynamicList() {
        return buildJudicialMessageDynamicList(null);
    }

    public List<Element<JudicialMessage>> getJudicialMessages() {
        return defaultIfNull(judicialMessages, new ArrayList<>());
    }

    @JsonUnwrapped
    @Builder.Default
    private final SyntheticCaseSummary syntheticCaseSummary = SyntheticCaseSummary.builder().build();

    public boolean hasSelectedTemporaryJudge(JudgeAndLegalAdvisor judge) {
        return judge.getJudgeTitle() != null;
    }

    @JsonUnwrapped
    @Builder.Default
    private final NoticeOfChangeAnswersData noticeOfChangeAnswersData = NoticeOfChangeAnswersData.builder().build();

    @JsonUnwrapped
    @Builder.Default
    private final RespondentPolicyData respondentPolicyData = RespondentPolicyData.builder().build();
}
