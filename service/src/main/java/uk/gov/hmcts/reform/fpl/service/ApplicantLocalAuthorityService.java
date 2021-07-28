package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthorityEventData;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.addMissingIds;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor
public class ApplicantLocalAuthorityService {

    private final PbaNumberService pbaNumberService;
    private final OrganisationService organisationService;
    private final ValidateEmailService validateEmailService;

    public LocalAuthority getUserLocalAuthority(CaseData caseData) {

        if (isEmpty(caseData.getLocalAuthorities())) {
            return migrateFromLegacyApplicant(caseData).orElseGet(this::getFromOrganisation);
        } else {
            return caseData.getLocalAuthorities().get(0).getValue();
        }
    }

    public void normalisePba(LocalAuthority localAuthority) {

        localAuthority.setPbaNumber(pbaNumberService.update(localAuthority.getPbaNumber()));
    }

    public List<String> validateLocalAuthority(LocalAuthority localAuthority) {

        final List<String> errors = new ArrayList<>();

        errors.addAll(pbaNumberService.validate(localAuthority.getPbaNumber()));
        errors.addAll(validateEmailService.validateIfPresent(localAuthority.getEmail()));

        return errors;
    }

    public List<String> validateColleagues(List<Element<Colleague>> colleagues) {

        final List<String> colleaguesEmails = unwrapElements(colleagues)
            .stream()
            .map(Colleague::getEmail)
            .collect(toList());

        return validateEmailService.validate(colleaguesEmails, "Colleague");
    }

    public List<Element<Colleague>> updateMainContact(LocalAuthorityEventData eventData) {

        final List<Element<Colleague>> colleagues = eventData.getLocalAuthorityColleagues();

        if (isEmpty(colleagues)) {
            return colleagues;
        }

        final UUID mainContactId = getMainContactId(eventData);

        colleagues.forEach(colleague -> {
            boolean isMainContact = Objects.equals(colleague.getId(), mainContactId);
            colleague.getValue().setMainContact(YesNo.from(isMainContact).getValue());
        });

        return colleagues;
    }

    public DynamicList buildContactsList(List<Element<Colleague>> colleagues) {

        final UUID mainContact = getMainContactId(colleagues);
        return asDynamicList(addMissingIds(colleagues), mainContact, Colleague::getFullName);
    }

    public List<String> getContactsEmails(CaseData caseData) {

        if (isNotEmpty(caseData.getLocalAuthorities())) {
            return caseData.getLocalAuthorities().stream()
                .map(Element::getValue)
                .flatMap(localAuthority -> localAuthority.getColleagues().stream())
                .map(Element::getValue)
                .filter(colleague -> colleague.getNotificationRecipient().equals("Yes"))
                .map(Colleague::getEmail)
                .filter(StringUtils::isNotBlank)
                .collect(toList());
        }

        return ofNullable(caseData.getSolicitor())
            .map(Solicitor::getEmail)
            .filter(StringUtils::isNotBlank)
            .map(List::of)
            .orElse(emptyList());
    }

    public List<Element<LocalAuthority>> save(CaseData caseData, LocalAuthorityEventData eventData) {

        final LocalAuthority localAuthority = eventData.getLocalAuthority();
        localAuthority.setColleagues(updateMainContact(eventData));

        final List<Element<LocalAuthority>> localAuthorities = caseData.getLocalAuthorities();

        if (isEmpty(localAuthorities)) {
            localAuthorities.add(element(localAuthority));
        } else {
            localAuthorities.get(0).setValue(localAuthority);
        }

        return localAuthorities;
    }

    private UUID getMainContactId(List<Element<Colleague>> colleagues) {

        return nullSafeList(colleagues).stream()
            .filter(colleague -> Objects.equals(colleague.getValue().getMainContact(), YES.getValue()))
            .map(Element::getId)
            .findFirst()
            .orElse(null);
    }

    private UUID getMainContactId(LocalAuthorityEventData eventData) {

        final List<Element<Colleague>> colleagues = eventData.getLocalAuthorityColleagues();

        if (isEmpty(colleagues)) {
            return null;
        }
        if (colleagues.size() == 1) {
            return colleagues.get(0).getId();
        }
        return ofNullable(eventData.getLocalAuthorityColleaguesList())
            .map(DynamicList::getValueCodeAsUUID)
            .orElse(null);
    }

    private Address getOrganisationAddress(Organisation organisation) {

        if (nonNull(organisation.getContactInformation())) {
            return organisation.getContactInformation().get(0).toAddress();
        }
        return Address.builder().build();
    }

    private Optional<LocalAuthority> migrateFromLegacyApplicant(CaseData caseData) {

        if (isEmpty(caseData.getAllApplicants())) {
            return Optional.empty();
        }

        final Optional<Applicant> legacyApplicant = ofNullable(caseData.getAllApplicants().get(0).getValue());
        final Optional<Solicitor> legacySolicitor = ofNullable(caseData.getSolicitor());

        return legacyApplicant
            .map(Applicant::getParty)
            .map(party -> LocalAuthority.builder()
                .name(party.getOrganisationName())
                .email(ofNullable(party.getEmail()).map(EmailAddress::getEmail).orElse(null))
                .legalTeamManager(party.getLegalTeamManager())
                .pbaNumber(party.getPbaNumber())
                .customerReference(party.getCustomerReference())
                .clientCode(party.getClientCode())
                .phone(ofNullable(firstNonNull(party.getTelephoneNumber(), party.getMobileNumber()))
                    .map(Telephone::getTelephoneNumber)
                    .orElse(null))
                .address(party.getAddress())
                .colleagues(legacySolicitor.map(this::migrateFromLegacySolicitor).orElse(null))
                .build());
    }

    private List<Element<Colleague>> migrateFromLegacySolicitor(Solicitor solicitor) {

        return Optional.ofNullable(solicitor).map(sol -> Colleague.builder()
            .role(SOLICITOR)
            .fullName(sol.getName())
            .email(sol.getEmail())
            .phone(Stream.of(sol.getTelephone(), sol.getMobile())
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .orElse(null))
            .dx(sol.getDx())
            .reference(sol.getReference())
            .notificationRecipient("Yes")
            .mainContact("Yes")
            .build())
            .map(ElementUtils::wrapElements)
            .orElse(null);
    }

    private LocalAuthority getFromOrganisation() {

        final Organisation organisation = organisationService.findOrganisation()
            .orElse(Organisation.builder().build());

        return LocalAuthority.builder()
            .id(organisation.getOrganisationIdentifier())
            .name(organisation.getName())
            .address(getOrganisationAddress(organisation))
            .build();
    }

}
