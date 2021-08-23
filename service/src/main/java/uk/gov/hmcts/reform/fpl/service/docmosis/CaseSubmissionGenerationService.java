package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderReasonsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.FactorsParenting;
import uk.gov.hmcts.reform.fpl.model.Grounds;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.HearingPreferences;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Proceeding;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.Risks;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApplicant;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisFactorsParenting;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearing;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingPreferences;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisInternationalElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisOtherParty;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisProceeding;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRisks;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.join;
import static java.time.LocalDate.parse;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.fromString;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.DONT_KNOW;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.AgeDisplayFormatHelper.formatAgeDisplay;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class CaseSubmissionGenerationService
    extends DocmosisTemplateDataGeneration<DocmosisCaseSubmission> {
    private static final String NEW_LINE = "\n";
    private static final String SPACE_DELIMITER = " ";
    private static final String DEFAULT_STRING = "-";
    private static final String CONFIDENTIAL = "Confidential";

    private final Time time;
    private final UserService userService;
    private final CourtService courtService;
    private final CaseSubmissionDocumentAnnexGenerator annexGenerator;

    public DocmosisCaseSubmission getTemplateData(final CaseData caseData) {
        DocmosisCaseSubmission.Builder applicationFormBuilder = DocmosisCaseSubmission.builder();

        applicationFormBuilder
            .applicantOrganisations(getApplicantsOrganisations(caseData))
            .respondentNames(getRespondentsNames(caseData.getAllRespondents()))
            .courtName(courtService.getCourtName(caseData))
            .submittedDate(formatDateDisplay(time.now().toLocalDate()))
            .ordersNeeded(getOrdersNeeded(caseData.getOrders()))
            .directionsNeeded(getDirectionsNeeded(caseData.getOrders()))
            .allocation(caseData.getAllocationProposal())
            .hearing(buildDocmosisHearing(caseData.getHearing()))
            .hearingPreferences(buildDocmosisHearingPreferences(caseData.getHearingPreferences()))
            .internationalElement(buildDocmosisInternationalElement(caseData.getInternationalElement()))
            .risks(buildDocmosisRisks(caseData.getRisks()))
            .factorsParenting(buildDocmosisFactorsParenting(caseData.getFactorsParenting()))
            .respondents(buildDocmosisRespondents(caseData.getAllRespondents()))
            .applicants(buildDocmosisApplicants(caseData))
            .children(buildDocmosisChildren(caseData.getAllChildren()))
            .others(buildDocmosisOthers(caseData.getAllOthers()))
            .proceeding(buildDocmosisProceedings(caseData.getAllProceedings()))
            .relevantProceedings(getValidAnswerOrDefaultValue(caseData.getRelevantProceedings()))
            .dischargeOfOrder(caseData.isDischargeOfCareApplication())
            .groundsForEPOReason(isNotEmpty(caseData.getOrders())
                ? getGroundsForEPOReason(caseData.getOrders().getOrderType(), caseData.getGroundsForEPO())
                : DEFAULT_STRING)
            .groundsThresholdReason(caseData.getGrounds() != null
                ? buildGroundsThresholdReason(caseData.getGrounds().getThresholdReason()) : DEFAULT_STRING)
            .thresholdDetails(getThresholdDetails(caseData.getGrounds()))
            .annexDocuments(annexGenerator.generate(caseData))
            .userFullName(getSigneeName(caseData));

        return applicationFormBuilder.build();
    }

    public void populateCaseNumber(final DocmosisCaseSubmission submittedCase, final long caseNumber) {
        submittedCase.setCaseNumber(String.valueOf(caseNumber));
    }

    public void populateDraftWaterOrCourtSeal(final DocmosisCaseSubmission caseSubmission, final boolean isDraft) {
        if (isDraft) {
            caseSubmission.setDraftWaterMark(getDraftWaterMarkData());
        } else {
            caseSubmission.setCourtSeal(getCourtSealData());
        }
    }

    private String getApplicantsOrganisations(CaseData caseData) {
        if (isNotEmpty(caseData.getLocalAuthorities())) {
            return unwrapElements(caseData.getLocalAuthorities()).stream()
                .map(LocalAuthority::getName)
                .filter(StringUtils::isNotBlank)
                .collect(joining(NEW_LINE));
        }

        return caseData.getAllApplicants().stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Applicant::getParty)
            .filter(Objects::nonNull)
            .map(ApplicantParty::getOrganisationName)
            .filter(StringUtils::isNotBlank)
            .collect(joining(NEW_LINE));
    }

    private String getRespondentsNames(final List<Element<Respondent>> respondents) {
        return respondents.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Respondent::getParty)
            .filter(Objects::nonNull)
            .map(RespondentParty::getFullName)
            .filter(StringUtils::isNotBlank)
            .collect(joining(NEW_LINE));
    }

    private String getOrdersNeeded(final Orders orders) {
        StringBuilder sb = new StringBuilder();

        if (orders != null && isNotEmpty(orders.getOrderType())) {
            sb.append(orders.getOrderType().stream()
                .map(OrderType::getLabel)
                .collect(joining(NEW_LINE)));

            sb.append(NEW_LINE);
            appendOtherOrderToOrdersNeeded(orders, sb);
            appendEmergencyProtectionOrdersAndDetailsToOrdersNeeded(orders, sb);
        }

        return StringUtils.isNotEmpty(sb.toString()) ? sb.toString().trim() : DEFAULT_STRING;
    }

    private void appendOtherOrderToOrdersNeeded(final Orders orders, final StringBuilder stringBuilder) {
        if (StringUtils.isNotEmpty(orders.getOtherOrder())) {
            stringBuilder.append(orders.getOtherOrder());
            stringBuilder.append(NEW_LINE);
        }
    }

    private void appendEmergencyProtectionOrdersAndDetailsToOrdersNeeded(final Orders orders,
                                                                         final StringBuilder stringBuilder) {
        if (isNotEmpty(orders.getEmergencyProtectionOrders())) {
            if (isNotEmpty(orders.getEpoType())) {
                stringBuilder.append(orders.getEpoType().getLabel());
                stringBuilder.append(NEW_LINE);
                if (orders.getEpoType() == PREVENT_REMOVAL) {
                    String address = orders.getAddress().getAddressAsString(NEW_LINE);
                    stringBuilder.append(address);
                    stringBuilder.append(NEW_LINE);
                }
            }
            stringBuilder.append(orders.getEmergencyProtectionOrders().stream()
                .map(EmergencyProtectionOrdersType::getLabel)
                .collect(joining(NEW_LINE)));

            stringBuilder.append(NEW_LINE);
            if (StringUtils.isNotEmpty(orders.getEmergencyProtectionOrderDetails())) {
                stringBuilder.append(orders.getEmergencyProtectionOrderDetails());
            }
        }
    }

    private String getDirectionsNeeded(final Orders orders) {
        StringBuilder stringBuilder = new StringBuilder();
        if (hasDirections(orders)) {
            if (isNotEmpty(orders.getEmergencyProtectionOrderDirections())) {
                stringBuilder.append(orders.getEmergencyProtectionOrderDirections().stream()
                    .map(EmergencyProtectionOrderDirectionsType::getLabel)
                    .collect(joining(NEW_LINE)));
            }

            stringBuilder.append(NEW_LINE);
            appendEmergencyProtectionOrderDirectionDetails(orders, stringBuilder);
            appendDirectionsAndDirectionDetails(orders, stringBuilder);
        }

        return StringUtils.isNotEmpty(stringBuilder.toString()) ? stringBuilder.toString().trim() : DEFAULT_STRING;
    }

    private boolean hasDirections(final Orders orders) {
        return isNotEmpty(orders) && (isNotEmpty(orders.getEmergencyProtectionOrderDirections())
            || StringUtils.isNotEmpty(orders.getDirections()));
    }

    private void appendEmergencyProtectionOrderDirectionDetails(final Orders orders,
                                                                final StringBuilder stringBuilder) {

        if (StringUtils.isNotBlank(orders.getExcluded())) {
            stringBuilder.append(orders.getExcluded() + " excluded");
            stringBuilder.append(NEW_LINE);
        }

        if (StringUtils.isNotEmpty(orders.getEmergencyProtectionOrderDirectionDetails())) {
            stringBuilder.append(orders.getEmergencyProtectionOrderDirectionDetails());
            stringBuilder.append(NEW_LINE);
        }
    }

    private void appendDirectionsAndDirectionDetails(final Orders orders, final StringBuilder stringBuilder) {
        if (StringUtils.isNotEmpty(orders.getDirections())) {
            stringBuilder.append(orders.getDirections());
            stringBuilder.append(NEW_LINE);
        }

        if (StringUtils.isNotEmpty(orders.getDirectionDetails())) {
            stringBuilder.append(orders.getDirectionDetails());
        }
    }

    private String getGroundsForEPOReason(final List<OrderType> orderTypes, final GroundsForEPO groundsForEPO) {
        if (isNotEmpty(orderTypes) && orderTypes.contains(OrderType.EMERGENCY_PROTECTION_ORDER)) {

            if (isNotEmpty(groundsForEPO) && isNotEmpty(groundsForEPO.getReason())) {
                return groundsForEPO.getReason()
                    .stream()
                    .map(reason -> EmergencyProtectionOrderReasonsType.valueOf(reason).getLabel())
                    .collect(joining(NEW_LINE + NEW_LINE));
            }

            return DEFAULT_STRING;
        }

        return EMPTY;
    }

    private String getThresholdDetails(final Grounds grounds) {
        return (isNotEmpty(grounds) && StringUtils.isNotEmpty(grounds.getThresholdDetails()))
            ? grounds.getThresholdDetails() : DEFAULT_STRING;
    }

    private String buildGroundsThresholdReason(final List<String> thresholdReasons) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isNotEmpty(thresholdReasons)) {
            thresholdReasons.forEach(thresholdReason -> {
                if ("noCare".equals(thresholdReason)) {
                    stringBuilder.append("Not receiving care that would be reasonably expected from a parent.");
                    stringBuilder.append(NEW_LINE);

                } else if ("beyondControl".equals(thresholdReason)) {
                    stringBuilder.append("Beyond parental control.");
                    stringBuilder.append(NEW_LINE);
                }
            });
        }

        return StringUtils.isNotEmpty(stringBuilder.toString()) ? stringBuilder.toString().trim() : DEFAULT_STRING;
    }

    public String getSigneeName(CaseData caseData) {
        return getLegalTeamManager(caseData)
            .filter(StringUtils::isNotBlank)
            .orElseGet(userService::getUserName);
    }

    private Optional<String> getLegalTeamManager(CaseData caseData) {
        if (isNotEmpty(caseData.getLocalAuthorities())) {
            return ofNullable(caseData.getDesignatedLocalAuthority())
                .map(LocalAuthority::getLegalTeamManager);
        }

        return ofNullable(caseData.getAllApplicants())
            .filter(ObjectUtils::isNotEmpty)
            .map(applicants -> applicants.get(0))
            .map(Element::getValue)
            .map(Applicant::getParty)
            .map(ApplicantParty::getLegalTeamManager);
    }

    private List<DocmosisRespondent> buildDocmosisRespondents(final List<Element<Respondent>> respondents) {
        return respondents.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Respondent::getParty)
            .filter(Objects::nonNull)
            .map(this::buildRespondent)
            .collect(toList());
    }

    private List<DocmosisApplicant> buildDocmosisApplicants(CaseData caseData) {

        if (isNotEmpty(caseData.getLocalAuthorities())) {
            return List.of(buildApplicant(caseData.getDesignatedLocalAuthority()));
        }

        final Solicitor legacySolicitor = caseData.getSolicitor();

        return unwrapElements(caseData.getApplicants()).stream()
            .filter(Objects::nonNull)
            .map(Applicant::getParty)
            .filter(Objects::nonNull)
            .map(applicant -> buildApplicant(applicant, legacySolicitor))
            .collect(toList());
    }

    private List<DocmosisChild> buildDocmosisChildren(final List<Element<Child>> children) {
        return children.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Child::getParty)
            .filter(Objects::nonNull)
            .map(this::buildChild)
            .collect(toList());
    }

    private List<DocmosisOtherParty> buildDocmosisOthers(final List<Element<Other>> other) {
        return other.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(this::buildOtherParty)
            .collect(toList());
    }

    private List<DocmosisProceeding> buildDocmosisProceedings(final List<Element<Proceeding>> proceedings) {
        return proceedings.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(this::buildProceeding)
            .collect(toList());
    }

    private DocmosisProceeding buildProceeding(final Proceeding proceeding) {
        return DocmosisProceeding.builder()
            .onGoingProceeding(getValidAnswerOrDefaultValue(proceeding.getOnGoingProceeding()))
            .proceedingStatus(getDefaultIfNullOrEmpty(proceeding.getProceedingStatus()))
            .caseNumber(getDefaultIfNullOrEmpty(proceeding.getCaseNumber()))
            .started(getDefaultIfNullOrEmpty(proceeding.getStarted()))
            .ended(getDefaultIfNullOrEmpty(proceeding.getEnded()))
            .ordersMade(getDefaultIfNullOrEmpty(proceeding.getOrdersMade()))
            .judge(getDefaultIfNullOrEmpty(proceeding.getJudge()))
            .children(getDefaultIfNullOrEmpty(proceeding.getChildren()))
            .guardian(getDefaultIfNullOrEmpty(proceeding.getGuardian()))
            .sameGuardianDetails(
                concatenateKeyAndValue(
                    proceeding.getSameGuardianNeeded(),
                    proceeding.getSameGuardianDetails()))
            .build();
    }

    private DocmosisOtherParty buildOtherParty(final Other other) {
        final boolean isConfidential = equalsIgnoreCase(other.getDetailsHidden(), YES.getValue());
        return DocmosisOtherParty.builder()
            .name(other.getName())
            .gender(formatGenderDisplay(other.getGender(), other.getGenderIdentification()))
            .dateOfBirth(StringUtils.isNotBlank(other.getDateOfBirth())
                ? formatLocalDateToString(parse(other.getDateOfBirth()), DATE)
                : DEFAULT_STRING
            )
            .placeOfBirth(getDefaultIfNullOrEmpty(other.getBirthPlace()))
            .address(isConfidential ? CONFIDENTIAL : formatAddress(other.getAddress()))
            .telephoneNumber(isConfidential ? CONFIDENTIAL : getDefaultIfNullOrEmpty(other.getTelephone()))
            .detailsHidden(getValidAnswerOrDefaultValue(other.getDetailsHidden()))
            .detailsHiddenReason(
                concatenateYesOrNoKeyAndValue(
                    other.getDetailsHidden(),
                    other.getDetailsHiddenReason()))
            .litigationIssuesDetails(
                concatenateYesOrNoKeyAndValue(
                    other.getLitigationIssues(),
                    other.getLitigationIssuesDetails()))
            .relationshipToChild(getDefaultIfNullOrEmpty(other.getChildInformation()))
            .build();
    }

    private DocmosisChild buildChild(final ChildParty child) {
        final boolean isConfidential = equalsIgnoreCase(child.getDetailsHidden(), YES.getValue());
        return DocmosisChild.builder()
            .name(child.getFullName())
            .age(formatAge(child.getDateOfBirth()))
            .gender(formatGenderDisplay(child.getGender(), child.getGenderIdentification()))
            .dateOfBirth(formatDateDisplay(child.getDateOfBirth()))
            .livingSituation(getChildLivingSituation(child, isConfidential))
            .keyDates(getDefaultIfNullOrEmpty(child.getKeyDates()))
            .careAndContactPlan(getDefaultIfNullOrEmpty(child.getCareAndContactPlan()))
            .adoption(getDefaultIfNullOrEmpty(child.getAdoption()))
            .placementOrderApplication(getValidAnswerOrDefaultValue(child.getPlacementOrderApplication()))
            .placementCourt(getDefaultIfNullOrEmpty(child.getPlacementCourt()))
            .mothersName(getDefaultIfNullOrEmpty(child.getMothersName()))
            .fathersName(getDefaultIfNullOrEmpty(child.getFathersName()))
            .fathersResponsibility(getDefaultIfNullOrEmpty(child.getFathersResponsibility()))
            .socialWorkerName(getDefaultIfNullOrEmpty(child.getSocialWorkerName()))
            .socialWorkerTelephoneNumber(getTelephoneNumber(child.getSocialWorkerTelephoneNumber()))
            .additionalNeeds(
                concatenateKeyAndValue(child.getAdditionalNeeds(), child.getAdditionalNeedsDetails()))
            .litigationIssues(
                concatenateYesOrNoKeyAndValue(child.getLitigationIssues(), child.getLitigationIssuesDetails()))
            .detailsHiddenReason(
                concatenateKeyAndValue(child.getDetailsHidden(), child.getDetailsHiddenReason()))
            .build();
    }

    private DocmosisRespondent buildRespondent(final RespondentParty respondent) {
        final boolean isConfidential = equalsIgnoreCase(respondent.getContactDetailsHidden(), YES.getValue());
        return DocmosisRespondent.builder()
            .name(respondent.getFullName())
            .age(formatAge(respondent.getDateOfBirth()))
            .gender(formatGenderDisplay(respondent.getGender(), respondent.getGenderIdentification()))
            .dateOfBirth(formatDateDisplay(respondent.getDateOfBirth()))
            .placeOfBirth(getDefaultIfNullOrEmpty(respondent.getPlaceOfBirth()))
            .address(
                isConfidential
                    ? CONFIDENTIAL
                    : formatAddress(respondent.getAddress()))
            .telephoneNumber(
                isConfidential
                    ? CONFIDENTIAL
                    : getDefaultIfNullOrEmpty(getTelephoneNumber(respondent.getTelephoneNumber())))
            .contactDetailsHidden(getValidAnswerOrDefaultValue(respondent.getContactDetailsHidden()))
            .contactDetailsHiddenDetails(
                concatenateYesOrNoKeyAndValue(
                    respondent.getContactDetailsHidden(),
                    respondent.getContactDetailsHiddenReason()))
            .litigationIssuesDetails(
                concatenateYesOrNoKeyAndValue(
                    respondent.getLitigationIssues(),
                    respondent.getLitigationIssuesDetails()))
            .relationshipToChild(getDefaultIfNullOrEmpty(respondent.getRelationshipToChild()))
            .build();
    }

    @Deprecated
    private DocmosisApplicant buildApplicant(final ApplicantParty applicant, final Solicitor solicitor) {
        final boolean solicitorPresent = (solicitor != null);
        return DocmosisApplicant.builder()
            .organisationName(getDefaultIfNullOrEmpty(applicant.getOrganisationName()))
            .contactName(getContactName(applicant.getTelephoneNumber()))
            .jobTitle(getDefaultIfNullOrEmpty(applicant.getJobTitle()))
            .address(formatAddress(applicant.getAddress()))
            .email(getEmail(applicant.getEmail()))
            .mobileNumber(getTelephoneNumber(applicant.getMobileNumber()))
            .telephoneNumber(getTelephoneNumber(applicant.getTelephoneNumber()))
            .pbaNumber(getDefaultIfNullOrEmpty(applicant.getPbaNumber()))
            .solicitorName(
                solicitorPresent ? getDefaultIfNullOrEmpty(solicitor.getName()) : DEFAULT_STRING)
            .solicitorMobile(
                solicitorPresent ? getDefaultIfNullOrEmpty(solicitor.getMobile()) : DEFAULT_STRING)
            .solicitorTelephone(
                solicitorPresent ? getDefaultIfNullOrEmpty(solicitor.getTelephone()) : DEFAULT_STRING)
            .solicitorEmail(
                solicitorPresent ? getDefaultIfNullOrEmpty(solicitor.getEmail()) : DEFAULT_STRING)
            .solicitorDx(
                solicitorPresent ? getDefaultIfNullOrEmpty(solicitor.getDx()) : DEFAULT_STRING)
            .solicitorReference(
                solicitorPresent ? getDefaultIfNullOrEmpty(solicitor.getReference()) : DEFAULT_STRING)
            .build();
    }

    private DocmosisApplicant buildApplicant(final LocalAuthority localAuthority) {
        final Optional<Colleague> solicitor = localAuthority.getFirstSolicitor();
        final Optional<Colleague> mainContact = localAuthority.getMainContact();

        return DocmosisApplicant.builder()
            .organisationName(getDefaultIfNullOrEmpty(localAuthority.getName()))
            .contactName(getDefaultIfNullOrEmpty(mainContact.map(Colleague::getFullName)))
            .jobTitle(getDefaultIfNullOrEmpty(mainContact.map(Colleague::getJobTitle)))
            .address(formatAddress(localAuthority.getAddress()))
            .email(getDefaultIfNullOrEmpty(localAuthority.getEmail()))
            .mobileNumber(getDefaultIfNullOrEmpty(mainContact.map(Colleague::getPhone)))
            .telephoneNumber(getDefaultIfNullOrEmpty(localAuthority.getPhone()))
            .pbaNumber(getDefaultIfNullOrEmpty(localAuthority.getPbaNumber()))
            .solicitorName(getDefaultIfNullOrEmpty(solicitor.map(Colleague::getFullName)))
            .solicitorMobile(getDefaultIfNullOrEmpty(solicitor.map(Colleague::getPhone)))
            .solicitorTelephone(getDefaultIfNullOrEmpty(solicitor.map(Colleague::getPhone)))
            .solicitorEmail(getDefaultIfNullOrEmpty(solicitor.map(Colleague::getEmail)))
            .solicitorDx(getDefaultIfNullOrEmpty(solicitor.map(Colleague::getDx)))
            .solicitorReference(getDefaultIfNullOrEmpty(solicitor.map(Colleague::getReference)))
            .build();
    }

    private String formatGenderDisplay(final String gender, final String genderIdentification) {
        if (StringUtils.isNotEmpty(gender)) {
            if ("They identify in another way".equalsIgnoreCase(gender)
                && StringUtils.isNotEmpty(genderIdentification)) {
                return genderIdentification;
            }
            return gender;
        }
        return DEFAULT_STRING;
    }

    private String getEmail(final EmailAddress email) {
        return email != null && StringUtils.isNotEmpty(email.getEmail()) ? email.getEmail() : DEFAULT_STRING;
    }

    private String getTelephoneNumber(final Telephone telephone) {
        return telephone != null && StringUtils.isNotEmpty(telephone.getTelephoneNumber())
            ? telephone.getTelephoneNumber() : DEFAULT_STRING;
    }

    private String getContactName(final Telephone telephone) {
        return telephone != null && StringUtils.isNotEmpty(telephone.getContactDirection())
            ? telephone.getContactDirection() : DEFAULT_STRING;
    }

    private String getDefaultIfNullOrEmpty(final String value) {
        return StringUtils.isEmpty(value) ? DEFAULT_STRING : value;
    }

    private String getDefaultIfNullOrEmpty(final Optional<String> value) {
        return value.filter(StringUtils::isNotEmpty).orElse(DEFAULT_STRING);
    }

    private String getChildLivingSituation(final ChildParty child, final boolean isConfidential) {
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotEmpty(child.getLivingSituation())) {
            stringBuilder.append(child.getLivingSituation());

            if (isConfidential) {
                stringBuilder.append(NEW_LINE).append(CONFIDENTIAL);
            } else if (isNotEmpty(child.getAddress())) {
                stringBuilder.append(NEW_LINE).append(child.getAddress().getAddressAsString(NEW_LINE));
            }

            stringBuilder.append(endsWith(stringBuilder.toString(), NEW_LINE) ? "" : NEW_LINE);
            formatChildLivingSituationDisplay(child, stringBuilder);
        }

        return StringUtils.isNotEmpty(stringBuilder.toString()) ? stringBuilder.toString().trim() : DEFAULT_STRING;
    }

    private void formatChildLivingSituationDisplay(final ChildParty child, final StringBuilder sb) {
        switch (fromString(child.getLivingSituation())) {
            case HOSPITAL_SOON_TO_BE_DISCHARGED:
                if (child.getDischargeDate() != null) {
                    sb.append("Discharge date: ").append(formatDateDisplay(child.getDischargeDate()));
                }
                break;
            case REMOVED_BY_POLICE_POWER_ENDS:
                if (child.getDatePowersEnd() != null) {
                    sb.append("Date powers end: ").append(formatDateDisplay(child.getDatePowersEnd()));
                }
                break;
            case VOLUNTARILY_SECTION_CARE_ORDER:
                if (child.getCareStartDate() != null) {
                    sb.append("Date this began: ").append(formatDateDisplay(child.getCareStartDate()));
                }
                break;
            default:
                if (child.getAddressChangeDate() != null) {
                    sb.append("Date this began: ")
                        .append(formatDateDisplay(child.getAddressChangeDate()));
                }
        }
    }

    private DocmosisHearing buildDocmosisHearing(final Hearing hearing) {
        final boolean hearingPresent = hearing != null;

        return DocmosisHearing.builder()
            .timeFrame(hearingPresent
                ? concatenateKeyAndValue(hearing.getTimeFrame(), addPrefixReason(hearing.getReason()))
                : DEFAULT_STRING)
            .typeAndReason(hearingPresent
                ? concatenateKeyAndValue(hearing.getType(), addPrefixReason(hearing.getTypeGiveReason()))
                : DEFAULT_STRING)
            .withoutNoticeDetails(hearingPresent
                ? concatenateKeyAndValue(hearing.getWithoutNotice(), addPrefixReason(hearing.getWithoutNoticeReason()))
                : DEFAULT_STRING)
            .reducedNoticeDetails(hearingPresent
                ? concatenateKeyAndValue(hearing.getReducedNotice(), addPrefixReason(hearing.getReducedNoticeReason()))
                : DEFAULT_STRING)
            .respondentsAware(hearingPresent && StringUtils.isNotEmpty(hearing.getRespondentsAware())
                ? hearing.getRespondentsAware()
                : DEFAULT_STRING)
            .respondentsAwareReason(hearingPresent && StringUtils.isNotEmpty(hearing.getRespondentsAware())
                ? hearing.getRespondentsAwareReason()
                : DEFAULT_STRING)
            .build();
    }

    private DocmosisHearingPreferences buildDocmosisHearingPreferences(final HearingPreferences hearingPreferences) {
        final boolean hearingPreferencesPresent = hearingPreferences != null;

        return DocmosisHearingPreferences.builder()
            .interpreter(hearingPreferencesPresent
                ? concatenateKeyAndValue(
                hearingPreferences.getInterpreter(),
                hearingPreferences.getInterpreterDetails()) : DEFAULT_STRING)
            .welshDetails(hearingPreferencesPresent
                ? concatenateKeyAndValue(
                hearingPreferences.getWelsh(),
                hearingPreferences.getWelshDetails()) : DEFAULT_STRING)
            .intermediary(hearingPreferencesPresent
                ? concatenateKeyAndValue(
                hearingPreferences.getIntermediary(),
                hearingPreferences.getIntermediaryDetails()) : DEFAULT_STRING)
            .disabilityAssistance(hearingPreferencesPresent
                ? concatenateKeyAndValue(
                hearingPreferences.getDisabilityAssistance(),
                hearingPreferences.getDisabilityAssistanceDetails()) : DEFAULT_STRING)
            .extraSecurityMeasures(hearingPreferencesPresent
                ? concatenateKeyAndValue(
                hearingPreferences.getExtraSecurityMeasures(),
                hearingPreferences.getExtraSecurityMeasuresDetails()) : DEFAULT_STRING)
            .somethingElse(hearingPreferencesPresent
                ? concatenateKeyAndValue(
                hearingPreferences.getSomethingElse(),
                hearingPreferences.getSomethingElseDetails()) : DEFAULT_STRING)
            .build();
    }

    private DocmosisRisks buildDocmosisRisks(final Risks risks) {
        final boolean risksPresent = (risks != null);
        return DocmosisRisks.builder()
            .neglectDetails(risksPresent
                ? concatenateYesOrNoKeyAndValue(
                risks.getNeglect(),
                listToString(risks.getNeglectOccurrences())) : DEFAULT_STRING)
            .sexualAbuseDetails(risksPresent
                ? concatenateYesOrNoKeyAndValue(
                risks.getSexualAbuse(),
                listToString(risks.getSexualAbuseOccurrences())) : DEFAULT_STRING)
            .physicalHarmDetails(risksPresent
                ? concatenateYesOrNoKeyAndValue(
                risks.getPhysicalHarm(),
                listToString(risks.getPhysicalHarmOccurrences())) : DEFAULT_STRING)
            .emotionalHarmDetails(risksPresent
                ? concatenateYesOrNoKeyAndValue(
                risks.getEmotionalHarm(),
                listToString(risks.getEmotionalHarmOccurrences())) : DEFAULT_STRING)
            .build();
    }

    private DocmosisFactorsParenting buildDocmosisFactorsParenting(
        final FactorsParenting factorsParenting) {
        final boolean factorsParentingPresent = (factorsParenting != null);

        return DocmosisFactorsParenting.builder()
            .alcoholDrugAbuseDetails(factorsParentingPresent
                ? concatenateYesOrNoKeyAndValue(
                factorsParenting.getAlcoholDrugAbuse(),
                factorsParenting.getAlcoholDrugAbuseReason()) : DEFAULT_STRING)
            .domesticViolenceDetails(factorsParentingPresent
                ? concatenateYesOrNoKeyAndValue(
                factorsParenting.getDomesticViolence(),
                factorsParenting.getDomesticViolenceReason()) : DEFAULT_STRING)
            .anythingElse(factorsParentingPresent
                ? concatenateYesOrNoKeyAndValue(
                factorsParenting.getAnythingElse(),
                factorsParenting.getAnythingElseReason()) : DEFAULT_STRING)
            .build();
    }

    private DocmosisInternationalElement buildDocmosisInternationalElement(
        final InternationalElement internationalElement) {
        final boolean internationalElementPresent = internationalElement != null;

        return DocmosisInternationalElement.builder()
            .possibleCarer(internationalElementPresent
                ? concatenateYesOrNoKeyAndValue(
                internationalElement.getPossibleCarer(),
                internationalElement.getPossibleCarerReason()) : DEFAULT_STRING)
            .significantEvents(internationalElementPresent
                ? concatenateYesOrNoKeyAndValue(
                internationalElement.getSignificantEvents(),
                internationalElement.getSignificantEventsReason()) : DEFAULT_STRING)
            .proceedings(internationalElementPresent
                ? concatenateYesOrNoKeyAndValue(
                internationalElement.getProceedings(),
                internationalElement.getProceedingsReason()) : DEFAULT_STRING)
            .internationalAuthorityInvolvement(internationalElementPresent
                ? concatenateYesOrNoKeyAndValue(
                internationalElement.getInternationalAuthorityInvolvement(),
                internationalElement.getInternationalAuthorityInvolvementDetails()) : DEFAULT_STRING)
            .issues(internationalElementPresent
                ? concatenateYesOrNoKeyAndValue(
                internationalElement.getIssues(),
                internationalElement.getIssuesReason()) : DEFAULT_STRING)
            .build();
    }

    private String concatenateKeyAndValue(final String key, final String value) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.isNotEmpty(key) ? key : DEFAULT_STRING);

        return StringUtils.isNotEmpty(value)
            ? sb.append(NEW_LINE).append(value).toString() : sb.toString();
    }

    private String concatenateYesOrNoKeyAndValue(final String key, final String value) {
        StringBuilder sb = new StringBuilder();
        sb.append(getValidAnswerOrDefaultValue(key));

        return (equalsIgnoreCase(key, YES.getValue()) && StringUtils.isNotEmpty(value))
            ? sb.append(NEW_LINE).append(value).toString() : sb.toString();
    }

    private String getValidAnswerOrDefaultValue(final String givenAnswer) {
        switch (YesNo.fromString(givenAnswer)) {
            case YES:
                return YES.getValue();
            case NO:
                return NO.getValue();
            case DONT_KNOW:
                return DONT_KNOW.getValue();
            default:
                return DEFAULT_STRING;
        }
    }

    private String listToString(final List<String> givenList) {
        return ofNullable(givenList)
            .map(list -> join(NEW_LINE, list))
            .orElse(EMPTY);
    }

    private String formatAddress(Address address) {
        return isNotEmpty(address) ? getDefaultIfNullOrEmpty(address.getAddressAsString(NEW_LINE)) : DEFAULT_STRING;
    }

    private String formatDateDisplay(final LocalDate dateToFormat) {
        return dateToFormat != null ? formatLocalDateToString(dateToFormat, DATE) : DEFAULT_STRING;
    }

    private String formatAge(final LocalDate dateOfBirth) {
        return dateOfBirth != null ? formatAgeDisplay(dateOfBirth) : DEFAULT_STRING;
    }

    private String addPrefixReason(String givenReason) {
        return isNotEmpty(givenReason)
            ? join(SPACE_DELIMITER, "Reason:", getDefaultIfNullOrEmpty(givenReason))
            : EMPTY;
    }
}
