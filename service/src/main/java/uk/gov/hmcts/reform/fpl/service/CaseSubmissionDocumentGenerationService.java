package uk.gov.hmcts.reform.fpl.service;

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
import uk.gov.hmcts.reform.fpl.model.Grounds;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisSubmittedForm;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class CaseSubmissionDocumentGenerationService extends DocmosisTemplateDataGeneration<DocmosisSubmittedForm> {
    private static final String NEW_LINE = "\n";
    private static final String DEFAULT_STRING = "-";
    private static final String BLANK_STRING = "";
    private static final LocalDate TODAY = LocalDate.now();

    private final UserDetailsService userDetailsService;

    public DocmosisSubmittedForm getTemplateData(final CaseData caseData) throws IOException {
        DocmosisSubmittedForm.Builder applicationFormBuilder = DocmosisSubmittedForm.builder();

        applicationFormBuilder
            .applicantOrganisations(getApplicantsOrganisations(caseData.getAllApplicants()))
            .respondentNames(getRespondentsNames(caseData.getAllRespondents()))
            .submittedDate(formatLocalDateToString(TODAY, DATE))
            .ordersNeeded(getOrdersNeeded(caseData.getOrders()))
            .directionsNeeded(getDirectionsNeeded(caseData.getOrders()))
            .allocation(caseData.getAllocationProposal())
            .hearing(caseData.getHearing())
            .hearingPreferences(caseData.getHearingPreferences())
            .internationalElement(caseData.getInternationalElement())
            .risks(caseData.getRisks())
            .factorsParenting(caseData.getFactorsParenting())
            .proceeding(caseData.getProceeding())
            .groundsForEPOReason(getGroundsForEPOReason(caseData.getOrders().getOrderType(),
                caseData.getGroundsForEPO()))
            .groundsThresholdReason(buildGroundsThresholdReason(caseData.getGrounds()))
            .thresholdDetails(getThresholdDetails(caseData.getGrounds()))
            .userFullName(userDetailsService.getUserName());
        applicationFormBuilder.courtseal(format(BASE_64, generateCourtSealEncodedString()));

        return applicationFormBuilder.build();
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

        return BLANK_STRING;
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
}
