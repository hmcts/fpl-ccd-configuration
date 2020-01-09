package uk.gov.hmcts.reform.fpl.service.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.exceptions.RoboticsDataException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
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
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.robotics.Gender.convertStringToGender;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoboticsDataService {
    private final DateFormatterService dateFormatterService;
    private final ObjectMapper objectMapper;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    public RoboticsData prepareRoboticsData(final CaseData caseData) {
        return RoboticsData.builder()
            .caseNumber(caseData.getFamilyManCaseNumber())
            .applicationType(deriveApplicationType(caseData.getOrders()))
            .feePaid(2055.00)
            .children(populateChildren(caseData.getAllChildren()))
            .respondents(populateRespondents(caseData.getRespondents1()))
            .solicitor(populateSolicitor(caseData.getSolicitor()))
            .harmAlleged(isNotEmpty(caseData.getRisks()))
            .internationalElement(isNotEmpty(caseData.getInternationalElement()))
            .allocation(isNotEmpty(caseData.getAllocationProposal())
                && isNotBlank(caseData.getAllocationProposal().getProposal())
                ? caseData.getAllocationProposal().getProposal() :  null)
            .issueDate(isNotEmpty(caseData.getDateSubmitted())
                ? dateFormatterService.formatLocalDateToString(caseData.getDateSubmitted(), "dd-MM-yyyy") : "")
            .applicant(populateApplicant(caseData.getAllApplicants()))
            .owningCourt(toInt(hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getCourtCode()))
            .build();
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
            uk.gov.hmcts.reform.fpl.model.ApplicantParty applicantParty = allApplicants.get(0).getValue().getParty();
            return Applicant.builder()
                .name(isBlank(applicantParty.getFullName()) ? null : applicantParty.getFullName())
                .contactName(getApplicantContactName(applicantParty.getTelephoneNumber()))
                .jobTitle(applicantParty.getJobTitle())
                .address(convertAddress(applicantParty.getAddress()))
                .mobileNumber(getApplicantPartyNumber(applicantParty.getMobileNumber()))
                .telephoneNumber(getApplicantPartyNumber(applicantParty.getTelephoneNumber()))
                .email(isNotEmpty(applicantParty.getEmail()) ? applicantParty.getEmail().getEmail() : null)
                .build();
        }

        return null;
    }

    private Address convertAddress(final uk.gov.hmcts.reform.fpl.model.Address address) {
        return Address.builder()
            .addressLine1(address.getAddressLine1())
            .addressLine2(address.getAddressLine2())
            .addressLine3(address.getAddressLine3())
            .postTown(address.getPostTown())
            .county(address.getCounty())
            .country(address.getCountry())
            .build();
    }

    private String getApplicantPartyNumber(final Telephone telephone) {
        return isNotEmpty(telephone) ? telephone.getTelephoneNumber() : null;
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
                .map(uk.gov.hmcts.reform.fpl.model.Respondent::getParty)
                .map(this::buildRespondent)
                .collect(toSet());
        }

        return of();
    }

    private Respondent buildRespondent(final RespondentParty respondentParty) {
        return Respondent.builder()
            .firstName(respondentParty.getFirstName())
            .lastName(respondentParty.getLastName())
            .gender(convertStringToGender(respondentParty.getGender()))
            .address(convertAddress(respondentParty.getAddress()))
            .relationshipToChild(respondentParty.getRelationshipToChild())
            .dob(formatDob(respondentParty.getDateOfBirth()))
            // TODO: 19/12/2019 verify if this should always be true ???
            .confidential(true)
            .build();
    }

    private Set<Child> populateChildren(final List<Element<uk.gov.hmcts.reform.fpl.model.Child>> allChildren) {
        if (isNotEmpty(allChildren)) {
            return allChildren.stream()
                .filter(Objects::nonNull)
                .map(Element::getValue)
                .filter(child -> isNotEmpty(child.getParty()))
                .map(uk.gov.hmcts.reform.fpl.model.Child::getParty)
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
            // TODO: 19/12/2019 verify if this should always be true ???
            .isParty(true)
            .build();
    }

    private String formatDob(final LocalDate date) {
        return isEmpty(date) ? "" : dateFormatterService.formatLocalDateToString(date, "d-MMM-y").toUpperCase();
    }

    private String deriveApplicationType(final Orders orders) {
        if (isEmpty(orders) && isEmpty(orders.getOrderType())) {
            throw new RoboticsDataException("No order type(s) to derive Application Type from.");
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
                return CARE_ORDER.getLabel();
            case SUPERVISION_ORDER:
            case INTERIM_SUPERVISION_ORDER:
                return SUPERVISION_ORDER.getLabel();
            case EMERGENCY_PROTECTION_ORDER:
                return EMERGENCY_PROTECTION_ORDER.getLabel();
            case EDUCATION_SUPERVISION_ORDER:
                return EDUCATION_SUPERVISION_ORDER.getLabel();
            case OTHER:
                return OTHER.getLabel();
        }

        throw new RoboticsDataException("Unable to derive an appropriate Application Type from " + orderType);
    }
}
