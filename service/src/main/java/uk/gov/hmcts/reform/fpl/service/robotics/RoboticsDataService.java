package uk.gov.hmcts.reform.fpl.service.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.exceptions.robotics.RoboticsDataException;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.Risks;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.robotics.Address;
import uk.gov.hmcts.reform.fpl.model.robotics.Applicant;
import uk.gov.hmcts.reform.fpl.model.robotics.Child;
import uk.gov.hmcts.reform.fpl.model.robotics.Respondent;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;
import uk.gov.hmcts.reform.fpl.model.robotics.Solicitor;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.robotics.Gender.convertStringToGender;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoboticsDataService {
    private final DateFormatterService dateFormatterService;
    private final ObjectMapper objectMapper;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final RoboticsDataValidatorService validatorService;

    public RoboticsData prepareRoboticsData(final CaseData caseData, final Long caseId) {
        final RoboticsData roboticsData = RoboticsData.builder()
            .caseNumber(caseData.getFamilyManCaseNumber())
            .applicationType(deriveApplicationType(caseData.getOrders()))
            .feePaid(2055.00)
            .children(populateChildren(caseData.getAllChildren()))
            .respondents(populateRespondents(caseData.getRespondents1()))
            .solicitor(populateSolicitor(caseData.getSolicitor()))
            .harmAlleged(hasRisks(caseData.getRisks()))
            .internationalElement(hasInternationalElement(caseData.getInternationalElement()))
            .allocation(isNotEmpty(caseData.getAllocationProposal())
                && isNotBlank(caseData.getAllocationProposal().getProposal())
                ? caseData.getAllocationProposal().getProposal() :  null)
            .issueDate(isNotEmpty(caseData.getDateSubmitted())
                ? dateFormatterService.formatLocalDateToString(caseData.getDateSubmitted(), "dd-MM-yyyy") : "")
            .applicant(populateApplicant(caseData.getAllApplicants()))
            .owningCourt(toInt(hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getCourtCode()))
            .caseId(caseId)
            .build();

        List<String> validationErrors = validatorService.validate(roboticsData);
        if (isNotEmpty(validationErrors)) {
            throw new RoboticsDataException(String.format("failed validation with these error(s) %s",
                validationErrors));
        }

        return roboticsData;
    }

    public String convertRoboticsDataToJson(final RoboticsData roboticsData) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(roboticsData);
        } catch (JsonProcessingException e) {
            throw new RoboticsDataException(e.getMessage(), e);
        }
    }

    private Applicant populateApplicant(final List<Element<uk.gov.hmcts.reform.fpl.model.Applicant>> allApplicants) {
        if (isNotEmpty(allApplicants)) {
            ApplicantParty applicantParty = allApplicants.get(0).getValue().getParty();
            return Applicant.builder()
                .name(applicantParty.getOrganisationName())
                .contactName(getApplicantContactName(applicantParty.getTelephoneNumber()))
                .jobTitle(applicantParty.getJobTitle())
                .address(convertAddress(applicantParty.getAddress()).orElse(null))
                .mobileNumber(getApplicantPartyNumber(applicantParty.getMobileNumber()))
                .telephoneNumber(getApplicantPartyNumber(applicantParty.getTelephoneNumber()))
                .email(isNotEmpty(applicantParty.getEmail()) ? applicantParty.getEmail().getEmail() : null)
                .build();
        }

        return null;
    }

    private Optional<Address> convertAddress(final uk.gov.hmcts.reform.fpl.model.Address address) {
        if (isNotEmpty(address)) {
            return Optional.of(Address.builder()
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .addressLine3(address.getAddressLine3())
                .postTown(address.getPostTown())
                .postcode(address.getPostcode())
                .county(address.getCounty())
                .country(address.getCountry())
                .build());
        }

        return Optional.empty();
    }

    private String getApplicantPartyNumber(final Telephone telephone) {
        return Optional.ofNullable(telephone)
            .map(Telephone::getTelephoneNumber)
            .filter(StringUtils::isNotBlank)
            .map(this::formatContactNumber)
            .orElse(null);
    }

    private String getApplicantContactName(final Telephone mobileNumber) {
        if (isEmpty(mobileNumber)) {
            return null;
        }

        return mobileNumber.getContactDirection();
    }

    private Solicitor populateSolicitor(final uk.gov.hmcts.reform.fpl.model.Solicitor solicitor) {
        if (isNotEmpty(solicitor) && isNotBlank(solicitor.getName())) {
            final String[] fullNameSplit = solicitor.getName().trim().split("\\s+");
            if (fullNameSplit.length > 1) {
                return Solicitor.builder()
                    .firstName(fullNameSplit[0])
                    .lastName(fullNameSplit[1])
                    .build();
            }
        }

        return null;
    }

    private Set<Respondent> populateRespondents(final List<Element<uk.gov.hmcts.reform.fpl.model.Respondent>>
                                                    respondents) {
        if (isNotEmpty(respondents)) {
            return respondents.stream()
                .filter(Objects::nonNull)
                .map(Element::getValue)
                .filter(respondent -> isNotEmpty(respondent.getParty()))
                .map(this::buildRespondent)
                .collect(toSet());
        }

        return of();
    }

    private Respondent buildRespondent(final uk.gov.hmcts.reform.fpl.model.Respondent respondent) {
        final RespondentParty respondentParty = respondent.getParty();

        return Respondent.builder()
            .firstName(respondentParty.getFirstName())
            .lastName(respondentParty.getLastName())
            .gender(convertStringToGender(respondentParty.getGender()))
            .address(convertAddress(respondentParty.getAddress()).orElse(null))
            .relationshipToChild(respondentParty.getRelationshipToChild())
            .dob(formatDob(respondentParty.getDateOfBirth()))
            .confidential(respondent.containsConfidentialDetails())
            .build();
    }

    private Set<Child> populateChildren(final List<Element<uk.gov.hmcts.reform.fpl.model.Child>> allChildren) {
        if (isNotEmpty(allChildren)) {
            return allChildren.stream()
                .filter(Objects::nonNull)
                .map(Element::getValue)
                .filter(child -> isNotEmpty(child.getParty()))
                .map(uk.gov.hmcts.reform.fpl.model.Child::getParty)
                .filter(Objects::nonNull)
                .map(this::buildChild)
                .collect(toSet());
        }

        return of();
    }

    private Child buildChild(final ChildParty childParty) {
        return Child.builder()
            .firstName(childParty.getFirstName())
            .lastName(childParty.getLastName())
            .gender(convertStringToGender(childParty.getGender()))
            .dob(formatDob(childParty.getDateOfBirth()))
            .isParty(false)
            .build();
    }

    private String formatDob(final LocalDate date) {
        return isEmpty(date) ? "" : dateFormatterService.formatLocalDateToString(date, "d-MMM-y").toUpperCase();
    }

    private String deriveApplicationType(final Orders orders) {
        if (isEmpty(orders) || ObjectUtils.isEmpty(orders.getOrderType())) {
            throw new RoboticsDataException("no order type(s) to derive Application Type from.");
        }

        List<OrderType> selectedOrderTypes = orders.getOrderType()
            .stream()
            .filter(Objects::nonNull)
            .collect(toList());

        if (selectedOrderTypes.size() > 1) {
            return selectedOrderTypes.stream()
                .map(this::getOrderTypeLabelValue)
                .distinct()
                .collect(joining(","));

        } else {
            return getOrderTypeLabelValue(selectedOrderTypes.get(0));
        }
    }

    private String getOrderTypeLabelValue(final OrderType orderType) {
        switch (orderType) {
            case CARE_ORDER:
            case INTERIM_CARE_ORDER:
                return "Care Order";
            case SUPERVISION_ORDER:
            case INTERIM_SUPERVISION_ORDER:
                return "Supervision Order";
            case EMERGENCY_PROTECTION_ORDER:
                return "Emergency Protection Order";
            case EDUCATION_SUPERVISION_ORDER:
                return "Education Supervision Order";
            case OTHER:
                return "Discharge of a Care Order";
        }

        throw new RoboticsDataException("unable to derive an appropriate Application Type from " + orderType);
    }

    private boolean hasInternationalElement(final InternationalElement internationalElement) {
        if (internationalElement == null) {
            return false;
        }

        return isAnyConfirmed(internationalElement.getPossibleCarer(), internationalElement.getSignificantEvents(),
            internationalElement.getIssues(), internationalElement.getProceedings(),
            internationalElement.getInternationalAuthorityInvolvement());
    }

    private boolean hasRisks(final Risks risks) {
        if (risks == null) {
            return false;
        }

        return isAnyConfirmed(risks.getPhysicalHarm(), risks.getEmotionalHarm(), risks.getSexualAbuse(),
            risks.getNeglect());
    }

    private boolean isAnyConfirmed(final String... values) {
        return asList(values).contains(YES.getValue());
    }

    private String formatContactNumber(final String number) {
        final String regEx = "(?!^)\\+|[^+\\d]+";

        return deleteWhitespace(number).replaceAll(regEx, "");
    }
}
