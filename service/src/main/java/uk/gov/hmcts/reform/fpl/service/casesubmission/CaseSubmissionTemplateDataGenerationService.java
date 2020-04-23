package uk.gov.hmcts.reform.fpl.service.casesubmission;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderReasonsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.DocmosisAnnexDocuments;
import uk.gov.hmcts.reform.fpl.model.DocmosisFactorsParenting;
import uk.gov.hmcts.reform.fpl.model.DocmosisHearing;
import uk.gov.hmcts.reform.fpl.model.DocmosisHearingPreferences;
import uk.gov.hmcts.reform.fpl.model.DocmosisInternationalElement;
import uk.gov.hmcts.reform.fpl.model.DocmosisRisks;
import uk.gov.hmcts.reform.fpl.model.FactorsParenting;
import uk.gov.hmcts.reform.fpl.model.Grounds;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.HearingPreferences;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.Risks;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisSubmittedForm;
import uk.gov.hmcts.reform.fpl.service.DocmosisTemplateDataGeneration;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class CaseSubmissionTemplateDataGenerationService extends DocmosisTemplateDataGeneration {
    private static final String NEW_LINE = "\n";
    private static final String DEFAULT_STRING = "-";
    private static final String CONFIDENTIAL = "Confidential";
    private static LocalDate TODAY = LocalDate.now();

    private final ObjectMapper objectMapper;
    private final UserDetailsService userDetailsService;

    public Map<String, Object> getTemplateData(final CaseData caseData, final boolean draft) throws IOException {
        DocmosisSubmittedForm.Builder applicationFormBuilder = DocmosisSubmittedForm.builder();

        applicationFormBuilder
            .applicantOrganisations(getApplicantsOrganisations(caseData.getAllApplicants()))
            .respondentNames(getRespondentsNames(caseData.getAllRespondents()))
            .submittedDate(formatLocalDateToString(TODAY, DATE))
            .ordersNeeded(getOrdersNeeded(caseData.getOrders()))
            .directionsNeeded(getDirectionsNeeded(caseData.getOrders()))
            .allocation(caseData.getAllocationProposal())
            .hearing(buildDocmosisHearing(caseData.getHearing()))
            .hearingPreferences(buildDocmosisHearingPreferences(caseData.getHearingPreferences()))
            .internationalElement(buildDocmosisInternationalElement(caseData.getInternationalElement()))
            .risks(buildDocmosisRisks(caseData.getRisks()))
            .factorsParenting(buildDocmosisFactorsParenting(caseData.getFactorsParenting()))
            .respondents(buildDocmosisRespondents(caseData.getAllRespondents()))
            .proceeding(caseData.getProceeding())
            .groundsForEPOReason(getGroundsForEPOReason(caseData.getOrders().getOrderType(),
                caseData.getGroundsForEPO()))
            .groundsThresholdReason(buildGroundsThresholdReason(caseData.getGrounds()))
            .thresholdDetails(getThresholdDetails(caseData.getGrounds()))
            .annexDocuments(buildDocmosisAnnexDocuments(caseData))
            .userFullName(userDetailsService.getUserName());

        if (draft) {
            applicationFormBuilder.draftWaterMark(format(BASE_64, generateDraftWatermarkEncodedString()));
        } else {
            applicationFormBuilder.courtseal(format(BASE_64, generateCourtSealEncodedString()));
        }

        return applicationFormBuilder.build().toMap(objectMapper);
    }

    private String getApplicantsOrganisations(final List<Element<Applicant>> applicants) {
        return applicants.stream()
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

            if (StringUtils.isNotEmpty(orders.getOtherOrder())) {
                sb.append(orders.getOtherOrder());
                sb.append(NEW_LINE);
            }

            if (isNotEmpty(orders.getEmergencyProtectionOrders())) {
                sb.append(orders.getEmergencyProtectionOrders().stream()
                    .map(EmergencyProtectionOrdersType::getLabel)
                    .collect(joining(NEW_LINE)));

                if (StringUtils.isNotEmpty(orders.getEmergencyProtectionOrderDetails())) {
                    sb.append(orders.getEmergencyProtectionOrderDetails());
                }
            }

        }

        return StringUtils.isNotEmpty(sb.toString()) ? sb.toString() : DEFAULT_STRING;
    }

    private String getDirectionsNeeded(final Orders orders) {
        StringBuilder sb = new StringBuilder();
        if (orders != null && (isNotEmpty(orders.getOrderType()) || StringUtils.isNotEmpty(orders.getDirections()))) {

            if (isNotEmpty(orders.getEmergencyProtectionOrderDirections())) {
                sb.append(orders.getEmergencyProtectionOrderDirections().stream()
                    .map(EmergencyProtectionOrderDirectionsType::getLabel)
                    .collect(joining(NEW_LINE)));
            }

            if (StringUtils.isNotEmpty(orders.getEmergencyProtectionOrderDirectionDetails())) {
                sb.append(orders.getEmergencyProtectionOrderDirectionDetails());
                sb.append(NEW_LINE);
            }

            if (StringUtils.isNotEmpty(orders.getDirections())) {
                sb.append(orders.getDirections());
                sb.append(NEW_LINE);
            }

            if (StringUtils.isNotEmpty(orders.getDirectionDetails())) {
                sb.append(orders.getDirectionDetails());
            }
        }

        return StringUtils.isNotEmpty(sb.toString()) ? sb.toString() : DEFAULT_STRING;
    }


    private String getGroundsForEPOReason(final List<OrderType> orderTypes, final GroundsForEPO groundsForEPO) {
        if (isNotEmpty(orderTypes) && orderTypes.contains(OrderType.EMERGENCY_PROTECTION_ORDER)) {

            if (isNotEmpty(groundsForEPO) && isNotEmpty(groundsForEPO.getReason())) {
                return groundsForEPO.getReason()
                    .stream()
                    .map(reason -> EmergencyProtectionOrderReasonsType.valueOf(reason).getLabel())
                    .collect(joining(NEW_LINE));
            }

            return DEFAULT_STRING;
        }

        return EMPTY;
    }

    private String getThresholdDetails(final Grounds grounds) {
        return (isNotEmpty(grounds) && StringUtils.isNotEmpty(grounds.getThresholdDetails()))
            ? grounds.getThresholdDetails() : DEFAULT_STRING;
    }

    private String buildGroundsThresholdReason(final Grounds grounds) {
        StringBuilder sb = new StringBuilder();
        if (isNotEmpty(grounds) && isNotEmpty(grounds.getThresholdReason())) {
            grounds.getThresholdReason().forEach(thresholdReason -> {
                if (StringUtils.equals(thresholdReason, "noCare")) {
                    sb.append("Not receiving care that would be reasonably expected from a parent.");
                    sb.append(NEW_LINE);

                } else if (StringUtils.equals(thresholdReason, "beyondControl")) {
                    sb.append("Beyond parental control.");
                    sb.append(NEW_LINE);
                }
            });
        }

        return StringUtils.isNotEmpty(sb.toString()) ? sb.toString() : DEFAULT_STRING;
    }

    private List<DocmosisRespondent> buildDocmosisRespondents(List<Element<Respondent>> respondents) {
        return respondents.stream()
            .map(element -> element.getValue().getParty())
            .map(this::buildRespondent)
            .collect(toList());
    }

    private DocmosisRespondent buildRespondent(RespondentParty respondent) {
        final boolean isConfidential = equalsIgnoreCase(respondent.getContactDetailsHidden(), YES.getValue());
        return DocmosisRespondent.builder()
            .name(respondent.getFullName())
            .age(getAge(respondent.getDateOfBirth()))
            .gender(displayGender(respondent.getGender(), respondent.getGenderIdentification()))
            .dateOfBirth(formatLocalDateToString(respondent.getDateOfBirth(), DATE))
            .placeOfBirth(getDefaultIfNull(respondent.getPlaceOfBirth()))
            .address(
                isConfidential ? CONFIDENTIAL : respondent.getAddress().getAddressAsString(NEW_LINE))
            .telephoneNumber(
                isConfidential ? CONFIDENTIAL : getTelephoneNumber(respondent.getTelephoneNumber()))
            .contactDetailsHidden(toYesOrNoOrDefaultValue(respondent.getContactDetailsHidden()))
            .contactDetailsHiddenDetails(
                concatenateYesOrNoKeyAndValue(
                    respondent.getContactDetailsHidden(),
                    respondent.getContactDetailsHiddenReason()))
            .litigationIssuesDetails(
                concatenateYesOrNoKeyAndValue(
                    respondent.getLitigationIssues(),
                    respondent.getLitigationIssuesDetails()))
            .relationshipToChild(getDefaultIfNull(respondent.getRelationshipToChild()))
            .build();
    }

    private String displayGender(String gender, String genderIdentification) {
        if (StringUtils.isNotEmpty(gender)) {
            if ((equalsIgnoreCase(gender, "They identify in another way") &&
                StringUtils.isNotEmpty(genderIdentification))) {
                return genderIdentification;
            }
            return gender;
        }
        return DEFAULT_STRING;
    }

    private String getTelephoneNumber(Telephone telephone) {
        return isNotEmpty(telephone) && StringUtils.isNotEmpty(telephone.getTelephoneNumber())
            ? telephone.getTelephoneNumber() : DEFAULT_STRING;
    }

    private String getDefaultIfNull(String value) {
        return StringUtils.isEmpty(value) ? DEFAULT_STRING : value;
    }

    private String getAge(LocalDate dateOfBirth) {
        Period period = Period.between(dateOfBirth, TODAY);
        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();
        if (years > 1) {
            return years + " years old";
        } else if (years == 1) {
            return years + " year old";
        } else if (months > 1) {
            return months + " months old";
        } else if (months == 1) {
            return months + " month old";
        } else if (days > 1) {
            return days + " days old";
        } else if (days == 1) {
            return days + " day old";
        } else {
            return "0 days old";
        }
    }

    private DocmosisHearing buildDocmosisHearing(
        final Hearing hearing) {
        final boolean hearingPresent = (hearing != null);
        return DocmosisHearing.builder()
            .timeFrame(hearingPresent
                ? concatenateKeyAndValue(
                hearing.getTimeFrame(),
                hearing.getReason()) : DEFAULT_STRING)
            .typeAndReason(hearingPresent
                ? concatenateKeyAndValue(
                hearing.getType(),
                hearing.getType_GiveReason()) : DEFAULT_STRING)
            .withoutNoticeDetails(hearingPresent
                ? concatenateKeyAndValue(
                hearing.getWithoutNotice(),
                hearing.getWithoutNoticeReason()) : DEFAULT_STRING)
            .reducedNoticeDetails(hearingPresent
                ? concatenateKeyAndValue(
                hearing.getReducedNotice(),
                hearing.getReducedNoticeReason()) : DEFAULT_STRING)
            .respondentsAware(
                hearingPresent && StringUtils.isNotEmpty(hearing.getRespondentsAware())
                    ? hearing.getRespondentsAware() : DEFAULT_STRING)
            .respondentsAwareReason(
                hearingPresent && StringUtils.isNotEmpty(hearing.getRespondentsAware())
                    ? hearing.getRespondentsAwareReason() : DEFAULT_STRING)
            .build();
    }

    private DocmosisAnnexDocuments buildDocmosisAnnexDocuments(final CaseData caseData) {
        return DocmosisAnnexDocuments.builder()
            .socialWorkChronology(getDisplayData(caseData.getSocialWorkChronologyDocument()))
            .socialWorkStatement(getDisplayData(caseData.getSocialWorkStatementDocument()))
            .socialWorkAssessment(getDisplayData(caseData.getSocialWorkAssessmentDocument()))
            .socialWorkCarePlan(getDisplayData(caseData.getSocialWorkCarePlanDocument()))
            .socialWorkEvidenceTemplate(getDisplayData(caseData.getSocialWorkEvidenceTemplateDocument()))
            .thresholdDocument(getDisplayData(caseData.getThresholdDocument()))
            .checklistDocument(getDisplayData(caseData.getChecklistDocument()))
            .others(getDisplayData(caseData.getOtherSocialWorkDocuments()))
            .build();
    }

    private String getDisplayData(Document document) {
        if (isNotEmpty(document) && StringUtils.isNotEmpty(document.getDocumentStatus())) {
            StringBuilder sb = new StringBuilder(document.getDocumentStatus());
            if (!equalsIgnoreCase(document.getDocumentStatus(), ATTACHED.getLabel())
                && StringUtils.isNotEmpty(document.getStatusReason())) {
                sb.append(NEW_LINE).append(document.getStatusReason());
            }
            return sb.toString();

        }
        return DEFAULT_STRING;
    }

    private List<DocmosisSocialWorkOther> getDisplayData(
        List<Element<DocumentSocialWorkOther>> otherSocialWorkDocuments) {
        return otherSocialWorkDocuments.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(this::buildDocmosisSocialWorkOther)
            .collect(toList());
    }

    private DocmosisSocialWorkOther buildDocmosisSocialWorkOther(DocumentSocialWorkOther document) {
        return DocmosisSocialWorkOther.builder()
            .documentTitle(
                StringUtils.isNotEmpty(document.getDocumentTitle()) ? document.getDocumentTitle() : DEFAULT_STRING)
            .build();
    }

    private DocmosisHearingPreferences buildDocmosisHearingPreferences(
        final HearingPreferences hearingPreferences) {
        final boolean hearingPreferencesPresent = (hearingPreferences != null);
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
        final boolean internationalElementPresent = (internationalElement != null);
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
        sb.append(toYesOrNoOrDefaultValue(key));

        return (equalsIgnoreCase(key, YES.getValue()) && StringUtils.isNotEmpty(value))
            ? sb.append(NEW_LINE).append(value).toString() : sb.toString();
    }

    private String toYesOrNoOrDefaultValue(final String yesOrNo) {
        if (equalsIgnoreCase(yesOrNo, YES.getValue())) {
            return YES.getValue();
        } else if (equalsIgnoreCase(yesOrNo, NO.getValue())) {
            return NO.getValue();
        }

        return DEFAULT_STRING;
    }

    private String listToString(final List<String> givenList) {
        return ofNullable(givenList)
            .map(list -> join(NEW_LINE, list))
            .orElse(EMPTY);
    }
}
