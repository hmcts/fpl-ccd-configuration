package uk.gov.hmcts.reform.fpl.service.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
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
import java.util.concurrent.atomic.AtomicReference;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.LOWER_CAMEL_CASE;
import static java.time.format.FormatStyle.MEDIUM;
import static java.util.Set.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.robotics.Gender.convertStringToGender;

@Slf4j
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
                ? defaultString(caseData.getAllocationProposal().getProposal()) :  "")
            .issueDate(isNotEmpty(caseData.getDateSubmitted())
                ? dateFormatterService.formatLocalDateToString(caseData.getDateSubmitted(), MEDIUM) : "")
            .applicant(populateApplicant(caseData.getAllApplicants()))
            .owningCourt(toInt(hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getCourtCode()))
            .build();
    }

    public String convertRoboticsDataToJson(final RoboticsData roboticsData) {
        try {
            return objectMapper.setPropertyNamingStrategy(LOWER_CAMEL_CASE)
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(roboticsData);
        } catch (JsonProcessingException e) {
            log.error("Unable to convert robotics data to json", e);
        }
        return "";
    }

    private Applicant populateApplicant(final List<Element<uk.gov.hmcts.reform.fpl.model.Applicant>> allApplicants) {
        final Applicant.ApplicantBuilder applicantBuilder = Applicant.builder();
        if (isNotEmpty(allApplicants)) {
            uk.gov.hmcts.reform.fpl.model.ApplicantParty applicantParty = allApplicants.get(0).getValue().getParty();
            return applicantBuilder
                .name(applicantParty.getFullName())
                .contactName(getApplicantContactName(applicantParty.getMobileNumber()))
                .jobTitle(applicantParty.getJobTitle())
                .address(convertAddress(applicantParty.getAddress()))
                .mobileNumber(getApplicantPartyNumber(applicantParty.getMobileNumber()))
                .telephoneNumber(getApplicantPartyNumber(applicantParty.getTelephoneNumber()))
                .email(isNotEmpty(applicantParty.getEmail()) ? defaultString(applicantParty.getEmail().getEmail()) : "")
                .build();
        }

        return applicantBuilder.build();
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
        return isNotEmpty(telephone) ? defaultString(telephone.getTelephoneNumber()) : "";
    }

    private String getApplicantContactName(final Telephone mobileNumber) {
        if (isEmpty(mobileNumber)) {
            return "";
        }

        return defaultString(mobileNumber.getContactDirection());
    }

    private Solicitor populateSolicitor(final uk.gov.hmcts.reform.fpl.model.Solicitor solicitor) {
        if (isNotEmpty(solicitor) && isNotBlank(solicitor.getName())) {
            final String[] fullNameSplit = solicitor.getName().trim().split("\\s+");
            return Solicitor.builder()
                .firstName(defaultString(fullNameSplit[0]))
                .lastName(defaultString(fullNameSplit[1]))
                .build();
        }

        return Solicitor.builder().build();
    }

    private Set<Respondent> populateRespondents(final List<Element<uk.gov.hmcts.reform.fpl.model.Respondent>>
                                                    respondents1) {
        if (isNotEmpty(respondents1)) {
            return respondents1.stream()
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
            .gender(respondentParty.getGender().toUpperCase())
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
            .gender(isNotEmpty(childParty.getGender()) ? convertStringToGender(childParty.getGender()) : "")
            .dob(formatDob(childParty.getDateOfBirth()))
            // TODO: 19/12/2019 verify if this should always be true ???
            .isParty(true)
            .build();
    }

    private String formatDob(final LocalDate date) {
        return isEmpty(date) ? "" : dateFormatterService.formatLocalDateToString(date, "d-MMM-y").toUpperCase();
    }

    private String deriveApplicationType(final Orders orders) {
        AtomicReference<String> applicationType = new AtomicReference<>();

        if (isNotEmpty(orders) && isNotEmpty(orders.getOrderType())) {
            List<OrderType> selectedOrderTypes = orders.getOrderType();
            if (selectedOrderTypes.size() > 1) {
                return selectedOrderTypes.stream()
                    .map(OrderType::getLabel)
                    .collect(joining(","));

            } else {
                selectedOrderTypes.forEach(orderType -> {
                    switch (orderType) {
                        case CARE_ORDER:
                        case INTERIM_CARE_ORDER:
                            applicationType.set(CARE_ORDER.getLabel());
                            break;
                        case SUPERVISION_ORDER:
                        case INTERIM_SUPERVISION_ORDER:
                            applicationType.set(SUPERVISION_ORDER.getLabel());
                            break;
                        case EMERGENCY_PROTECTION_ORDER:
                            applicationType.set(EMERGENCY_PROTECTION_ORDER.getLabel());
                            break;
                        case EDUCATION_SUPERVISION_ORDER:
                            applicationType.set(EDUCATION_SUPERVISION_ORDER.getLabel());
                            break;
                        case OTHER:
                            applicationType.set(OTHER.getLabel());
                    }
                });
            }
        }

        return applicationType.get();
    }
}
