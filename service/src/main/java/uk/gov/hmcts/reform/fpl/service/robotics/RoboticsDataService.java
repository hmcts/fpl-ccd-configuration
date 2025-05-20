package uk.gov.hmcts.reform.fpl.service.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.AddressNotKnowReason;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.exceptions.robotics.RoboticsDataException;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
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
import uk.gov.hmcts.reform.fpl.service.CourtService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.anyNotNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.robotics.Gender.convertStringToGender;
import static uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper.fromCCDMoneyGBP;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoboticsDataService {
    private final ObjectMapper objectMapper;
    private final CourtService courtService;

    public RoboticsData prepareRoboticsData(final CaseData caseData) {
        return RoboticsData.builder()
            .caseNumber(caseData.getFamilyManCaseNumber())
            .applicationType(deriveApplicationType(caseData.getOrders()))
            .feePaid(fromCCDMoneyGBP(caseData.getAmountToPay()).orElse(BigDecimal.valueOf(2055.00)))
            .children(populateChildren(caseData.getAllChildren()))
            .respondents(populateRespondents(caseData.getRespondents1()))
            .solicitor(populateSolicitor(caseData))
            .harmAlleged(hasRisks(caseData.getRisks()))
            .internationalElement(hasInternationalElement(caseData.getInternationalElement()))
            .allocation(isNotEmpty(caseData.getAllocationProposal())
                && isNotBlank(caseData.getAllocationProposal().getProposalV2())
                ? caseData.getAllocationProposal().getProposalV2() : null)
            .issueDate(formatDate(caseData.getDateSubmitted(), "dd-MM-yyyy"))
            .applicant(populateApplicant(caseData))
            .owningCourt(toInt(courtService.getCourtCode(caseData)))
            .caseId(caseData.getId())
            .build();
    }

    public String convertRoboticsDataToJson(final RoboticsData roboticsData) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(roboticsData);
        } catch (JsonProcessingException e) {
            throw new RoboticsDataException(e.getMessage(), e);
        }
    }

    private Applicant populateApplicant(final CaseData caseData) {
        final LocalAuthority localAuthority = caseData.getDesignatedLocalAuthority();
        if (isNotEmpty(localAuthority)) {
            final Optional<Colleague> mainContact = localAuthority.getMainContact();

            return Applicant.builder()
                .name(localAuthority.getName())
                .contactName(mainContact.map(Colleague::buildFullName).orElse(null))
                .jobTitle(mainContact.map(Colleague::getJobTitle).orElse(null))
                .address(convertAddress(localAuthority.getAddress()).orElse(null))
                .mobileNumber(mainContact
                    .map(Colleague::getPhone)
                    .map(this::formatContactNumber)
                    .orElse(null))
                .telephoneNumber(ofNullable(localAuthority.getPhone())
                    .map(this::formatContactNumber)
                    .orElse(null))
                .email(localAuthority.getEmail())
                .build();
        }

        if (isNotEmpty(caseData.getAllApplicants())) {
            final ApplicantParty legacyApplicantParty = caseData.getAllApplicants().get(0).getValue().getParty();

            return Applicant.builder()
                .name(legacyApplicantParty.getOrganisationName())
                .contactName(getApplicantContactName(legacyApplicantParty.getTelephoneNumber()))
                .jobTitle(legacyApplicantParty.getJobTitle())
                .address(convertAddress(legacyApplicantParty.getAddress()).orElse(null))
                .mobileNumber(getApplicantPartyNumber(legacyApplicantParty.getMobileNumber()))
                .telephoneNumber(getApplicantPartyNumber(legacyApplicantParty.getTelephoneNumber()))
                .email(isNotEmpty(legacyApplicantParty.getEmail()) ? legacyApplicantParty.getEmail().getEmail() : null)
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
        return ofNullable(telephone)
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

    private Solicitor populateSolicitor(final CaseData caseData) {
        return getSolicitorName(caseData)
            .filter(StringUtils::isNotBlank)
            .map(String::trim)
            .map(solicitor -> solicitor.split("\\s+"))
            .filter(nameParts -> nameParts.length > 1)
            .map(nameParts -> Solicitor.builder()
                .firstName(nameParts[0])
                .lastName(nameParts[1])
                .build())
            .orElse(null);
    }

    private Optional<String> getSolicitorName(CaseData caseData) {
        LocalAuthority designatedLA = caseData.getDesignatedLocalAuthority();
        if (isNotEmpty(designatedLA)) {
            return designatedLA
                .getFirstSolicitor()
                .map(Colleague::buildFullName);
        }
        return ofNullable(caseData.getSolicitor())
            .map(uk.gov.hmcts.reform.fpl.model.Solicitor::getName);
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
            .addressNotKnowReason(
                AddressNotKnowReason.fromType(
                    respondentParty.getAddressNotKnowReason()
                ).map(AddressNotKnowReason::getType)
                    .orElse(null))
            .relationshipToChild(respondentParty.getRelationshipToChild())
            .dob(formatDate(respondentParty.getDateOfBirth(), "d-MMM-y"))
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
            .gender(convertStringToGender(
                Optional.ofNullable(childParty.getGender()).map(ChildGender::getLabel).orElse(null)))
            .dob(formatDate(childParty.getDateOfBirth(), "d-MMM-y"))
            .isParty(false)
            .build();
    }

    private String formatDate(LocalDate date, String format) {
        return ofNullable(date)
            .map(dateToFormat -> formatLocalDateToString(dateToFormat, format).toUpperCase())
            .orElse(null);
    }

    private String deriveApplicationType(final Orders orders) {
        if (isEmpty(orders) || isEmpty(orders.getOrderType())) {
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
            default:
                return orderType.getLabel();
        }
    }

    private boolean hasInternationalElement(final InternationalElement internationalElement) {
        if (internationalElement == null) {
            return false;
        }

        return isAnyConfirmed(internationalElement.getWhichCountriesInvolved(),
            internationalElement.getOutsideHagueConvention(), internationalElement.getImportantDetails());
    }

    private boolean hasRisks(final Risks risks) {
        if (risks == null) {
            return false;
        }

        return anyNotNull(risks.getWhatKindOfRiskAndHarmToChildren(), 
            risks.getFactorsAffectingParenting(), risks.getAnythingElseAffectingParenting());
    }

    private boolean isAnyConfirmed(final String... values) {
        return asList(values).contains(YES.getValue());
    }

    private String formatContactNumber(final String number) {
        final String regEx = "(?!^)\\+|[^+\\d]+";

        return deleteWhitespace(number).replaceAll(regEx, "");
    }

}
