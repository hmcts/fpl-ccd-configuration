package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderReasonsType;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
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
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
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
import uk.gov.hmcts.reform.fpl.model.robotics.Gender;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.join;
import static java.time.LocalDate.parse;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
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
    private static final Map<TranslationSection, LanguagePair> translations = Map.of(
        TranslationSection.EXCLUDED, LanguagePair.of(" excluded", " wedi'u heithrio"),
        TranslationSection.BEYOND_PARENTAL_CONTROL, LanguagePair.of(
            "Beyond parental control.", "Y tu hwnt i reolaeth rhiant."
        ),
        TranslationSection.NOT_RECEIVING_CARE, LanguagePair.of(
            "Not receiving care that would be reasonably expected from a parent.",
            "Ddim yn derbyn y gofal a fyddai'n rhesymol ddisgwyliedig gan riant."
        ),
        TranslationSection.REASON, LanguagePair.of("Reason:", "Rheswm:"),
        TranslationSection.CONFIDENTIAL, LanguagePair.of("Confidential", "Cyfrinachol")
    );

    private final Time time;
    private final UserService userService;
    private final CourtService courtService;
    private final CaseSubmissionDocumentAnnexGenerator annexGenerator;

    public DocmosisCaseSubmission getTemplateData(final CaseData caseData) {
        Language applicationLanguage = Optional.ofNullable(caseData.getC110A()
            .getLanguageRequirementApplication()).orElse(Language.ENGLISH);

        return DocmosisCaseSubmission.builder()
            .applicantOrganisations(getApplicantsOrganisations(caseData))
            .respondentNames(getRespondentsNames(caseData.getAllRespondents()))
            .courtName(courtService.getCourtName(caseData))
            .submittedDate(formatDateDisplay(time.now().toLocalDate(), applicationLanguage))
            .ordersNeeded(getOrdersNeeded(caseData.getOrders(), applicationLanguage))
            .directionsNeeded(getDirectionsNeeded(caseData.getOrders(), applicationLanguage))
            .allocation(caseData.getAllocationProposal())
            .hearing(buildDocmosisHearing(caseData.getHearing(), applicationLanguage))
            .welshLanguageRequirement(getWelshLanguageRequirement(caseData, applicationLanguage))
            .hearingPreferences(buildDocmosisHearingPreferences(caseData.getHearingPreferences(), applicationLanguage))
            .internationalElement(buildDocmosisInternationalElement(caseData.getInternationalElement(),
                applicationLanguage))
            .risks(buildDocmosisRisks(caseData.getRisks(), applicationLanguage))
            .factorsParenting(buildDocmosisFactorsParenting(caseData.getFactorsParenting(), applicationLanguage))
            .respondents(buildDocmosisRespondents(caseData.getAllRespondents(), applicationLanguage))
            .applicants(buildDocmosisApplicants(caseData))
            .children(buildDocmosisChildren(caseData.getAllChildren(), applicationLanguage))
            .others(buildDocmosisOthers(caseData.getAllOthers(), applicationLanguage))
            .proceeding(buildDocmosisProceedings(caseData.getAllProceedings(), applicationLanguage))
            .relevantProceedings(getValidAnswerOrDefaultValue(caseData.getRelevantProceedings(), applicationLanguage))
            .groundsForEPOReason(isNotEmpty(caseData.getOrders())
                                 ? getGroundsForEPOReason(caseData.getOrders().getOrderType(),
                caseData.getGroundsForEPO(),
                applicationLanguage)
                                 : DEFAULT_STRING)
            .groundsThresholdReason(caseData.getGrounds() != null
                                    ? buildGroundsThresholdReason(caseData.getGrounds().getThresholdReason(),
                applicationLanguage) : DEFAULT_STRING)
            .thresholdDetails(getThresholdDetails(caseData.getGrounds()))
            .annexDocuments(annexGenerator.generate(caseData, applicationLanguage))
            .userFullName(getSigneeName(caseData))
            .build();
    }

    public void populateCaseNumber(final DocmosisCaseSubmission submittedCase, final long caseNumber) {
        submittedCase.setCaseNumber(String.valueOf(caseNumber));
    }

    public void populateDraftWaterOrCourtSeal(final DocmosisCaseSubmission caseSubmission, final boolean isDraft,
                                              Language imageLanguage) {
        if (isDraft) {
            caseSubmission.setDraftWaterMark(getDraftWaterMarkData());
        } else {
            caseSubmission.setCourtSeal(getCourtSealData(imageLanguage));
        }
    }

    public String getSigneeName(CaseData caseData) {
        return getLegalTeamManager(caseData)
            .filter(StringUtils::isNotBlank)
            .orElseGet(userService::getUserName);
    }

    private String getWelshLanguageRequirement(CaseData caseData,
                                               Language applicationLanguage) {
        return YesNo.fromString(defaultIfBlank(caseData.getLanguageRequirement(), "No")).getValue(applicationLanguage);
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

    private String getOrdersNeeded(final Orders orders,
                                   Language applicationLanguage) {
        StringBuilder sb = new StringBuilder();

        if (orders != null && isNotEmpty(orders.getOrderType())) {
            sb.append(orders.getOrderType().stream()
                .map(orderType -> orderType.getLabel(applicationLanguage))
                .collect(joining(NEW_LINE)));

            sb.append(NEW_LINE);
            appendOtherOrderToOrdersNeeded(orders, sb);
            appendEmergencyProtectionOrdersAndDetailsToOrdersNeeded(orders, sb, applicationLanguage);
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
                                                                         final StringBuilder stringBuilder,
                                                                         Language applicationLanguage) {
        if (isNotEmpty(orders.getEmergencyProtectionOrders())) {
            if (isNotEmpty(orders.getEpoType())) {
                stringBuilder.append(orders.getEpoType().getLabel(applicationLanguage));
                stringBuilder.append(NEW_LINE);
                if (orders.getEpoType() == PREVENT_REMOVAL) {
                    String address = orders.getAddress().getAddressAsString(NEW_LINE);
                    stringBuilder.append(address);
                    stringBuilder.append(NEW_LINE);
                }
            }
            stringBuilder.append(orders.getEmergencyProtectionOrders().stream()
                .map(emergencyProtectionOrdersType -> emergencyProtectionOrdersType.getLabel(applicationLanguage))
                .collect(joining(NEW_LINE)));

            stringBuilder.append(NEW_LINE);
            if (StringUtils.isNotEmpty(orders.getEmergencyProtectionOrderDetails())) {
                stringBuilder.append(orders.getEmergencyProtectionOrderDetails());
            }
        }
    }

    private String getDirectionsNeeded(final Orders orders,
                                       Language applicationLanguage) {
        StringBuilder stringBuilder = new StringBuilder();
        if (hasDirections(orders)) {
            if (isNotEmpty(orders.getEmergencyProtectionOrderDirections())) {
                stringBuilder.append(orders.getEmergencyProtectionOrderDirections().stream()
                    .map(emergencyProtectionOrderDirectionsType ->
                        emergencyProtectionOrderDirectionsType.getLabel(applicationLanguage))
                    .collect(joining(NEW_LINE)));
            }

            stringBuilder.append(NEW_LINE);
            appendEmergencyProtectionOrderDirectionDetails(orders, stringBuilder, applicationLanguage);
            appendDirectionsAndDirectionDetails(orders, stringBuilder, applicationLanguage);
        }

        return StringUtils.isNotEmpty(stringBuilder.toString()) ? stringBuilder.toString().trim() : DEFAULT_STRING;
    }

    private boolean hasDirections(final Orders orders) {
        return isNotEmpty(orders) && (isNotEmpty(orders.getEmergencyProtectionOrderDirections())
                                      || StringUtils.isNotEmpty(orders.getDirections()));
    }

    private void appendEmergencyProtectionOrderDirectionDetails(final Orders orders,
                                                                final StringBuilder stringBuilder,
                                                                Language applicationLanguage) {

        if (StringUtils.isNotBlank(orders.getExcluded())) {
            stringBuilder.append(orders.getExcluded())
                .append(translations.get(TranslationSection.EXCLUDED).fromLanguage(applicationLanguage));
            stringBuilder.append(NEW_LINE);
        }

        if (StringUtils.isNotEmpty(orders.getEmergencyProtectionOrderDirectionDetails())) {
            stringBuilder.append(orders.getEmergencyProtectionOrderDirectionDetails());
            stringBuilder.append(NEW_LINE);
        }
    }

    private void appendDirectionsAndDirectionDetails(final Orders orders, final StringBuilder stringBuilder,
                                                     Language applicationLanguage) {
        if (StringUtils.isNotEmpty(orders.getDirections())) {
            stringBuilder.append(getValidAnswerOrDefaultValue(orders.getDirections(), applicationLanguage));
            stringBuilder.append(NEW_LINE);
        }

        if (StringUtils.isNotEmpty(orders.getDirectionDetails())) {
            stringBuilder.append(orders.getDirectionDetails());
        }
    }

    private String getGroundsForEPOReason(final List<OrderType> orderTypes, final GroundsForEPO groundsForEPO,
                                          Language applicationLanguage) {
        if (isNotEmpty(orderTypes) && orderTypes.contains(OrderType.EMERGENCY_PROTECTION_ORDER)) {

            if (isNotEmpty(groundsForEPO) && isNotEmpty(groundsForEPO.getReason())) {
                return groundsForEPO.getReason()
                    .stream()
                    .map(reason -> EmergencyProtectionOrderReasonsType.valueOf(reason).getLabel(applicationLanguage))
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

    private String buildGroundsThresholdReason(final List<String> thresholdReasons,
                                               Language applicationLanguage) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isNotEmpty(thresholdReasons)) {
            thresholdReasons.forEach(thresholdReason -> {
                if ("noCare".equals(thresholdReason)) {
                    stringBuilder.append(getThresholdReasonNoCare(applicationLanguage));
                    stringBuilder.append(NEW_LINE);

                } else if ("beyondControl".equals(thresholdReason)) {
                    stringBuilder.append(getThresholdReasonBeyondControl(applicationLanguage));
                    stringBuilder.append(NEW_LINE);
                }
            });
        }

        return StringUtils.isNotEmpty(stringBuilder.toString()) ? stringBuilder.toString().trim() : DEFAULT_STRING;
    }

    private String getThresholdReasonBeyondControl(Language applicationLanguage) {
        return translations.get(TranslationSection.BEYOND_PARENTAL_CONTROL).fromLanguage(applicationLanguage);
    }

    private String getThresholdReasonNoCare(Language applicationLanguage) {
        return translations.get(TranslationSection.NOT_RECEIVING_CARE).fromLanguage(applicationLanguage);
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

    private List<DocmosisRespondent> buildDocmosisRespondents(final List<Element<Respondent>> respondents,
                                                              Language applicationLanguage) {
        return respondents.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Respondent::getParty)
            .filter(Objects::nonNull)
            .map(respondent -> buildRespondent(respondent, applicationLanguage))
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

    private List<DocmosisChild> buildDocmosisChildren(final List<Element<Child>> children,
                                                      Language applicationLanguage) {
        return children.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Child::getParty)
            .filter(Objects::nonNull)
            .map(child -> buildChild(child, applicationLanguage))
            .collect(toList());
    }

    private List<DocmosisOtherParty> buildDocmosisOthers(final List<Element<Other>> other,
                                                         Language applicationLanguage) {
        return other.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(other1 -> buildOtherParty(other1, applicationLanguage))
            .collect(toList());
    }

    private List<DocmosisProceeding> buildDocmosisProceedings(final List<Element<Proceeding>> proceedings,
                                                              Language applicationLanguage) {
        return proceedings.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(proceeding -> buildProceeding(proceeding, applicationLanguage))
            .collect(toList());
    }

    private DocmosisProceeding buildProceeding(final Proceeding proceeding,
                                               Language applicationLanguage) {
        return DocmosisProceeding.builder()
            .onGoingProceeding(getValidAnswerOrDefaultValue(proceeding.getOnGoingProceeding(), applicationLanguage))
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

    private DocmosisOtherParty buildOtherParty(final Other other,
                                               Language applicationLanguage) {
        final boolean isConfidential = equalsIgnoreCase(other.getDetailsHidden(), YES.getValue());
        return DocmosisOtherParty.builder()
            .name(other.getName())
            .gender(formatGenderDisplay(Gender.fromLabel(other.getGender()).getLabel(applicationLanguage),
                other.getGenderIdentification()))
            .dateOfBirth(StringUtils.isNotBlank(other.getDateOfBirth())
                         ? formatLocalDateToString(parse(other.getDateOfBirth()), DATE, applicationLanguage)
                         : DEFAULT_STRING
            )
            .placeOfBirth(getDefaultIfNullOrEmpty(other.getBirthPlace()))
            .address(isConfidential ? getConfidential(applicationLanguage) : formatAddress(other.getAddress()))
            .telephoneNumber(isConfidential ? getConfidential(applicationLanguage) :
                             getDefaultIfNullOrEmpty(other.getTelephone()))
            .detailsHidden(getValidAnswerOrDefaultValue(other.getDetailsHidden(), applicationLanguage))
            .detailsHiddenReason(
                concatenateYesOrNoKeyAndValue(
                    other.getDetailsHidden(),
                    other.getDetailsHiddenReason(), applicationLanguage))
            .litigationIssuesDetails(
                concatenateYesOrNoKeyAndValue(
                    other.getLitigationIssues(),
                    other.getLitigationIssuesDetails(), applicationLanguage))
            .relationshipToChild(getDefaultIfNullOrEmpty(other.getChildInformation()))
            .build();
    }

    private DocmosisChild buildChild(final ChildParty child,
                                     Language applicationLanguage) {
        final boolean isConfidential = equalsIgnoreCase(child.getDetailsHidden(), YES.getValue());
        return DocmosisChild.builder()
            .name(child.getFullName())
            .age(formatAge(child.getDateOfBirth(), applicationLanguage))
            .gender(formatGenderDisplay(
                ChildGender.fromLabel(child.getGender()).getLabel(applicationLanguage),
                child.getGenderIdentification()))
            .dateOfBirth(formatDateDisplay(child.getDateOfBirth(), applicationLanguage))
            .livingSituation(getChildLivingSituation(child, isConfidential, applicationLanguage))
            .keyDates(getDefaultIfNullOrEmpty(child.getKeyDates()))
            .careAndContactPlan(getDefaultIfNullOrEmpty(child.getCareAndContactPlan()))
            .adoption(getValidAnswerOrDefaultValue(child.getAdoption(), applicationLanguage))
            .placementOrderApplication(getValidAnswerOrDefaultValue(child.getPlacementOrderApplication(),
                applicationLanguage))
            .placementCourt(getDefaultIfNullOrEmpty(child.getPlacementCourt()))
            .mothersName(getDefaultIfNullOrEmpty(child.getMothersName()))
            .fathersName(getDefaultIfNullOrEmpty(child.getFathersName()))
            .fathersResponsibility(getValidAnswerOrDefaultValue(child.getFathersResponsibility(), applicationLanguage))
            .socialWorkerName(getDefaultIfNullOrEmpty(child.getSocialWorkerName()))
            .socialWorkerTelephoneNumber(getTelephoneNumber(child.getSocialWorkerTelephoneNumber()))
            .additionalNeeds(
                concatenateYesOrNoKeyAndValue(child.getAdditionalNeeds(),
                    child.getAdditionalNeedsDetails(),
                    applicationLanguage))
            .litigationIssues(
                concatenateYesOrNoKeyAndValue(child.getLitigationIssues(), child.getLitigationIssuesDetails(),
                    applicationLanguage))
            .detailsHiddenReason(
                concatenateKeyAndValue(child.getDetailsHidden(), child.getDetailsHiddenReason()))
            .build();
    }

    private DocmosisRespondent buildRespondent(final RespondentParty respondent,
                                               Language applicationLanguage) {
        final boolean isConfidential = equalsIgnoreCase(respondent.getContactDetailsHidden(), YES.getValue());
        return DocmosisRespondent.builder()
            .name(respondent.getFullName())
            .age(formatAge(respondent.getDateOfBirth(), applicationLanguage))
            .gender(formatGenderDisplay(Gender.fromLabel(respondent.getGender()).getLabel(applicationLanguage),
                respondent.getGenderIdentification()))
            .dateOfBirth(formatDateDisplay(respondent.getDateOfBirth(), applicationLanguage))
            .placeOfBirth(getDefaultIfNullOrEmpty(respondent.getPlaceOfBirth()))
            .address(
                isConfidential
                ? getConfidential(applicationLanguage)
                : formatAddress(respondent.getAddress()))
            .telephoneNumber(
                isConfidential
                ? getConfidential(applicationLanguage)
                : getDefaultIfNullOrEmpty(getTelephoneNumber(respondent.getTelephoneNumber())))
            .contactDetailsHidden(getValidAnswerOrDefaultValue(respondent.getContactDetailsHidden(),
                applicationLanguage))
            .contactDetailsHiddenDetails(
                concatenateYesOrNoKeyAndValue(
                    respondent.getContactDetailsHidden(),
                    respondent.getContactDetailsHiddenReason(), applicationLanguage))
            .litigationIssuesDetails(
                concatenateYesOrNoKeyAndValue(
                    respondent.getLitigationIssues(),
                    respondent.getLitigationIssuesDetails(), applicationLanguage))
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
            if ("Maent yn uniaethu mewn ffordd arall".equalsIgnoreCase(gender)
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

    private String getChildLivingSituation(final ChildParty child, final boolean isConfidential,
                                           Language applicationLanguage) {
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotEmpty(child.getLivingSituation())) {
            stringBuilder.append(child.getLivingSituation());

            if (isConfidential) {
                stringBuilder.append(NEW_LINE).append(getConfidential(applicationLanguage));
            } else if (isNotEmpty(child.getAddress())) {
                stringBuilder.append(NEW_LINE).append(child.getAddress().getAddressAsString(NEW_LINE));
            }

            stringBuilder.append(endsWith(stringBuilder.toString(), NEW_LINE) ? "" : NEW_LINE);
            formatChildLivingSituationDisplay(child, stringBuilder, applicationLanguage);
        }

        return StringUtils.isNotEmpty(stringBuilder.toString()) ? stringBuilder.toString().trim() : DEFAULT_STRING;
    }

    private void formatChildLivingSituationDisplay(final ChildParty child, final StringBuilder sb,
                                                   Language applicationLanguage) {
        switch (fromString(child.getLivingSituation())) {
            case HOSPITAL_SOON_TO_BE_DISCHARGED:
                if (child.getDischargeDate() != null) {
                    if (applicationLanguage.equals(Language.ENGLISH)) {
                        sb.append("Discharge date: ")
                            .append(formatDateDisplay(child.getDischargeDate(), applicationLanguage));
                    } else {
                        sb.append("Dyddiad diddymu: ")
                            .append(formatDateDisplay(child.getDischargeDate(), applicationLanguage));
                    }
                }
                break;
            case REMOVED_BY_POLICE_POWER_ENDS:
                if (child.getDatePowersEnd() != null) {
                    if (applicationLanguage.equals(Language.ENGLISH)) {
                        sb.append("Date powers end: ")
                            .append(formatDateDisplay(child.getDatePowersEnd(), applicationLanguage));
                    } else {
                        sb.append("Dyddiad y daw’r pwerau i ben: ")
                            .append(formatDateDisplay(child.getDatePowersEnd(), applicationLanguage));
                    }
                }
                break;
            case VOLUNTARILY_SECTION_CARE_ORDER:
                if (child.getCareStartDate() != null) {
                    if (applicationLanguage.equals(Language.ENGLISH)) {
                        sb.append("Date this began: ")
                            .append(formatDateDisplay(child.getCareStartDate(), applicationLanguage));
                    } else {
                        sb.append("Dyddiad y bu i hyn gychwyn: ")
                            .append(formatDateDisplay(child.getCareStartDate(), applicationLanguage));
                    }
                }
                break;
            default:
                if (child.getAddressChangeDate() != null) {

                    if (applicationLanguage.equals(Language.ENGLISH)) {
                        sb.append("Date this began: ")
                            .append(formatDateDisplay(child.getAddressChangeDate(), applicationLanguage));
                    } else {
                        sb.append("Dyddiad y bu i hyn gychwyn: ")
                            .append(formatDateDisplay(child.getAddressChangeDate(), applicationLanguage));
                    }
                }
        }
    }

    private DocmosisHearing buildDocmosisHearing(final Hearing hearing,
                                                 Language applicationLanguage) {
        final boolean hearingPresent = hearing != null;

        return DocmosisHearing.builder()
            .timeFrame(hearingPresent
                       ? concatenateKeyAndValue(hearing.getTimeFrame(),
                addPrefixReason(hearing.getReason(), applicationLanguage))
                       : DEFAULT_STRING)
            .typeAndReason(hearingPresent
                           ? concatenateKeyAndValue(hearing.getType(), addPrefixReason(hearing.getTypeGiveReason(),
                applicationLanguage))
                           : DEFAULT_STRING)
            .withoutNoticeDetails(hearingPresent
                                  ? concatenateYesOrNoKeyAndValue(hearing.getWithoutNotice(),
                addPrefixReason(hearing.getWithoutNoticeReason(),
                    applicationLanguage),
                applicationLanguage)
                                  : DEFAULT_STRING)
            .reducedNoticeDetails(hearingPresent
                                  ? concatenateYesOrNoKeyAndValue(hearing.getReducedNotice(),
                addPrefixReason(hearing.getReducedNoticeReason(),
                    applicationLanguage),
                applicationLanguage)
                                  : DEFAULT_STRING)
            .respondentsAware(hearingPresent && StringUtils.isNotEmpty(hearing.getRespondentsAware())
                              ? YesNo.fromString(hearing.getRespondentsAware()).getValue(applicationLanguage)
                              : DEFAULT_STRING)
            .respondentsAwareReason(hearingPresent && StringUtils.isNotEmpty(hearing.getRespondentsAware())
                                    ? hearing.getRespondentsAwareReason()
                                    : DEFAULT_STRING)
            .build();
    }

    private DocmosisHearingPreferences buildDocmosisHearingPreferences(final HearingPreferences hearingPreferences,
                                                                       Language applicationLanguage) {
        final boolean hearingPreferencesPresent = hearingPreferences != null;

        return DocmosisHearingPreferences.builder()
            .interpreter(hearingPreferencesPresent
                         ? concatenateYesOrNoKeyAndValue(
                hearingPreferences.getInterpreter(),
                hearingPreferences.getInterpreterDetails(), applicationLanguage) : DEFAULT_STRING)
            .welshDetails(hearingPreferencesPresent
                          ? concatenateYesOrNoKeyAndValue(
                hearingPreferences.getWelsh(),
                hearingPreferences.getWelshDetails(), applicationLanguage) : DEFAULT_STRING)
            .intermediary(hearingPreferencesPresent
                          ? concatenateYesOrNoKeyAndValue(
                hearingPreferences.getIntermediary(),
                hearingPreferences.getIntermediaryDetails(), applicationLanguage) : DEFAULT_STRING)
            .disabilityAssistance(hearingPreferencesPresent
                                  ? concatenateYesOrNoKeyAndValue(
                hearingPreferences.getDisabilityAssistance(),
                hearingPreferences.getDisabilityAssistanceDetails(), applicationLanguage) : DEFAULT_STRING)
            .extraSecurityMeasures(hearingPreferencesPresent
                                   ? concatenateYesOrNoKeyAndValue(
                hearingPreferences.getExtraSecurityMeasures(),
                hearingPreferences.getExtraSecurityMeasuresDetails(), applicationLanguage) : DEFAULT_STRING)
            .somethingElse(hearingPreferencesPresent
                           ? concatenateYesOrNoKeyAndValue(
                hearingPreferences.getSomethingElse(),
                hearingPreferences.getSomethingElseDetails(), applicationLanguage) : DEFAULT_STRING)
            .build();
    }

    private DocmosisRisks buildDocmosisRisks(final Risks risks,
                                             Language applicationLanguage) {
        final boolean risksPresent = (risks != null);
        return DocmosisRisks.builder()
            .neglectDetails(risksPresent
                            ? concatenateYesOrNoKeyAndValue(
                risks.getNeglect(),
                listToString(risks.getNeglectOccurrences()), applicationLanguage) : DEFAULT_STRING)
            .sexualAbuseDetails(risksPresent
                                ? concatenateYesOrNoKeyAndValue(
                risks.getSexualAbuse(),
                listToString(risks.getSexualAbuseOccurrences()), applicationLanguage) : DEFAULT_STRING)
            .physicalHarmDetails(risksPresent
                                 ? concatenateYesOrNoKeyAndValue(
                risks.getPhysicalHarm(),
                listToString(risks.getPhysicalHarmOccurrences()), applicationLanguage) : DEFAULT_STRING)
            .emotionalHarmDetails(risksPresent
                                  ? concatenateYesOrNoKeyAndValue(
                risks.getEmotionalHarm(),
                listToString(risks.getEmotionalHarmOccurrences()), applicationLanguage) : DEFAULT_STRING)
            .build();
    }

    private DocmosisFactorsParenting buildDocmosisFactorsParenting(final FactorsParenting factorsParenting,
                                                                   Language applicationLanguage) {
        final boolean factorsParentingPresent = (factorsParenting != null);

        return DocmosisFactorsParenting.builder()
            .alcoholDrugAbuseDetails(factorsParentingPresent
                                     ? concatenateYesOrNoKeyAndValue(
                factorsParenting.getAlcoholDrugAbuse(),
                factorsParenting.getAlcoholDrugAbuseReason(), applicationLanguage) : DEFAULT_STRING)
            .domesticViolenceDetails(factorsParentingPresent
                                     ? concatenateYesOrNoKeyAndValue(
                factorsParenting.getDomesticViolence(),
                factorsParenting.getDomesticViolenceReason(), applicationLanguage) : DEFAULT_STRING)
            .anythingElse(factorsParentingPresent
                          ? concatenateYesOrNoKeyAndValue(
                factorsParenting.getAnythingElse(),
                factorsParenting.getAnythingElseReason(), applicationLanguage) : DEFAULT_STRING)
            .build();
    }

    private DocmosisInternationalElement buildDocmosisInternationalElement(InternationalElement internationalElement,
                                                                           Language applicationLanguage) {
        final boolean internationalElementPresent = internationalElement != null;

        return DocmosisInternationalElement.builder()
            .possibleCarer(internationalElementPresent
                           ? concatenateYesOrNoKeyAndValue(
                internationalElement.getPossibleCarer(),
                internationalElement.getPossibleCarerReason(), applicationLanguage) : DEFAULT_STRING)
            .significantEvents(internationalElementPresent
                               ? concatenateYesOrNoKeyAndValue(
                internationalElement.getSignificantEvents(),
                internationalElement.getSignificantEventsReason(), applicationLanguage) : DEFAULT_STRING)
            .proceedings(internationalElementPresent
                         ? concatenateYesOrNoKeyAndValue(
                internationalElement.getProceedings(),
                internationalElement.getProceedingsReason(), applicationLanguage) : DEFAULT_STRING)
            .internationalAuthorityInvolvement(internationalElementPresent
                                               ? concatenateYesOrNoKeyAndValue(
                internationalElement.getInternationalAuthorityInvolvement(),
                internationalElement.getInternationalAuthorityInvolvementDetails(),
                applicationLanguage) : DEFAULT_STRING)
            .issues(internationalElementPresent
                    ? concatenateYesOrNoKeyAndValue(
                internationalElement.getIssues(),
                internationalElement.getIssuesReason(), applicationLanguage) : DEFAULT_STRING)
            .build();
    }

    private String concatenateKeyAndValue(final String key, final String value) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.isNotEmpty(key) ? key : DEFAULT_STRING);

        return StringUtils.isNotEmpty(value)
               ? sb.append(NEW_LINE).append(value).toString() : sb.toString();
    }

    private String concatenateYesOrNoKeyAndValue(final String key, final String value,
                                                 Language applicationLanguage) {
        StringBuilder sb = new StringBuilder();
        sb.append(getValidAnswerOrDefaultValue(key, applicationLanguage));

        return (equalsIgnoreCase(key, YES.getValue()) && StringUtils.isNotEmpty(value))
               ? sb.append(NEW_LINE).append(value).toString() : sb.toString();
    }

    private String getValidAnswerOrDefaultValue(final String givenAnswer,
                                                Language applicationLanguage) {

        switch (YesNo.fromString(givenAnswer)) {
            case YES:
                return YES.getValue(applicationLanguage);
            case NO:
                return NO.getValue(applicationLanguage);
            case DONT_KNOW:
                return DONT_KNOW.getValue(applicationLanguage);
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

    private String formatDateDisplay(final LocalDate dateToFormat, Language language) {
        return dateToFormat != null ? formatLocalDateToString(dateToFormat, DATE, language) : DEFAULT_STRING;
    }

    private String formatAge(final LocalDate dateOfBirth,
                             Language applicationLanguage) {
        return dateOfBirth != null ? formatAgeDisplay(dateOfBirth, applicationLanguage) : DEFAULT_STRING;
    }

    private String addPrefixReason(String givenReason,
                                   Language applicationLanguage) {
        String reason = translations.get(TranslationSection.REASON).fromLanguage(applicationLanguage);
        return isNotEmpty(givenReason)
               ? join(SPACE_DELIMITER, reason, getDefaultIfNullOrEmpty(givenReason))
               : EMPTY;
    }

    private String getConfidential(Language applicationLanguage) {
        return translations.get(TranslationSection.CONFIDENTIAL).fromLanguage(applicationLanguage);
    }

    private enum TranslationSection {
        EXCLUDED,
        BEYOND_PARENTAL_CONTROL,
        NOT_RECEIVING_CARE,
        REASON,
        CONFIDENTIAL
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class LanguagePair {
        private final String english;
        private final String welsh;

        public static LanguagePair of(String english, String welsh) {
            return new LanguagePair(english, welsh);
        }

        public String fromLanguage(Language language) {
            return Language.ENGLISH == language ? english : welsh;
        }
    }
}
