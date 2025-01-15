package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityIdLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.exceptions.OrganisationNotFound;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
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
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOCIAL_WORKER;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class ApplicantLocalAuthorityService {

    private final PbaNumberService pbaNumberService;
    private final OrganisationService organisationService;
    private final ValidateEmailService validateEmailService;
    private final LocalAuthorityIdLookupConfiguration localAuthorityIds;
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmails;

    public LocalAuthority getUserLocalAuthority(CaseData caseData) {

        final Organisation organisation = getOrganisation(caseData);

        return findLocalAuthority(caseData, organisation.getOrganisationIdentifier())
            .map(Element::getValue)
            .orElseGet(() -> migrateFromLegacyApplicant(caseData, organisation.getOrganisationIdentifier())
                .orElseGet(() -> getLocalAuthority(organisation)));
    }

    public Colleague getMainContact(LocalAuthority localAuthority) {
        return localAuthority.getMainContact()
            .map(this::migrateFromLegacyColleague)
            .orElse(Colleague.builder().build());
    }

    public List<Element<Colleague>> getOtherContact(LocalAuthority localAuthority) {
        return localAuthority.getOtherContact().stream()
            .map(colleague -> element(colleague.getId(),
                migrateFromLegacyColleague(colleague.getValue())))
            .toList();
    }

    private Colleague migrateFromLegacyColleague(Colleague colleague) {
        return colleague.toBuilder()
            .role((SOCIAL_WORKER.equals(colleague.getRole())) ? OTHER : colleague.getRole())
            .title((SOCIAL_WORKER.equals(colleague.getRole())) ? SOCIAL_WORKER.getLabel() : colleague.getTitle())
            .dx(null)
            .reference(null)
            .notificationRecipient(null)
            .build();
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

    public List<String> validateColleagues(List<Colleague> colleagues) {

        final List<String> colleaguesEmails = colleagues.stream()
            .map(Colleague::getEmail)
            .collect(toList());

        return validateEmailService.validate(colleaguesEmails, "Colleague");
    }

    public List<String> getDesignatedLocalAuthorityContactsEmails(CaseData caseData) {

        if (isNotEmpty(caseData.getLocalAuthorities())) {
            return getDesignatedLocalAuthority(caseData).getContactEmails();
        }

        return ofNullable(caseData.getSolicitor())
            .map(Solicitor::getEmail)
            .filter(StringUtils::isNotBlank)
            .map(List::of)
            .orElse(emptyList());
    }

    public List<Element<LocalAuthority>> save(CaseData caseData, LocalAuthorityEventData eventData) {

        final LocalAuthority editedLocalAuthority = eventData.getLocalAuthority();
        final String userOrgId = editedLocalAuthority.getId();

        editedLocalAuthority.setColleagues(buildColleagueList(eventData));

        final List<Element<LocalAuthority>> localAuthorities = caseData.getLocalAuthorities();

        findLocalAuthority(caseData, userOrgId).ifPresentOrElse(
            laElement -> laElement.setValue(editedLocalAuthority),
            () -> localAuthorities.add(element(editedLocalAuthority)));

        return updateDesignatedLocalAuthority(caseData);
    }

    private Optional<UUID> getMainContactId(LocalAuthority localAuthority) {
        return localAuthority.getMainContactElement().map(Element::getId);
    }

    private List<Element<Colleague>> buildColleagueList(LocalAuthorityEventData eventData) {
        return Stream.concat(
            Stream.of(element(getMainContactId(eventData.getLocalAuthority()).orElse(UUID.randomUUID()),
                eventData.getApplicantContact().toBuilder().mainContact(YES.getValue()).build())),
            eventData.getApplicantContactOthers().stream()
                .map(otherContact -> element(otherContact.getId(),
                    otherContact.getValue().toBuilder().mainContact(NO.getValue()).build())))
            .toList();
    }


    private Address getOrganisationAddress(Organisation organisation) {

        if (nonNull(organisation.getContactInformation())) {
            return organisation.getContactInformation().get(0).toAddress();
        }
        return Address.builder().build();
    }

    private Optional<LocalAuthority> migrateFromLegacyApplicant(CaseData caseData, String organisationId) {

        if (isEmpty(caseData.getAllApplicants())) {
            return empty();
        }

        final Optional<String> designatedOrgId = getDesignatedOrgId(caseData);

        if (designatedOrgId.isEmpty()) {
            return empty();
        }

        if (!designatedOrgId.get().equals(organisationId)) {
            return empty();
        }

        final Optional<Applicant> legacyApplicant = ofNullable(caseData.getAllApplicants().get(0).getValue());
        final Optional<Solicitor> legacySolicitor = ofNullable(caseData.getSolicitor());

        return legacyApplicant
            .map(Applicant::getParty)
            .map(party -> LocalAuthority.builder()
                .id(designatedOrgId.get())
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
                .colleagues(legacySolicitor.map(this::migrateFromLegacySolicitor).orElse(emptyList()))
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

    public LocalAuthority getLocalAuthority(Organisation organisation) {

        final String sharedInbox = localAuthorityIds.getLocalAuthorityCode(organisation.getOrganisationIdentifier())
            .flatMap(localAuthorityEmails::getSharedInbox)
            .orElse(null);

        return LocalAuthority.builder()
            .id(organisation.getOrganisationIdentifier())
            .name(organisation.getName())
            .email(sharedInbox)
            .address(getOrganisationAddress(organisation))
            .build();
    }

    public List<Element<LocalAuthority>> updateDesignatedLocalAuthority(CaseData caseData) {

        final String designatedOrgId = getDesignatedOrgId(caseData).orElse(null);

        caseData.getLocalAuthorities()
            .stream()
            .map(Element::getValue)
            .forEach(la -> la.setDesignated(YesNo.from(Objects.equals(la.getId(), designatedOrgId)).getValue()));

        return caseData.getLocalAuthorities();
    }

    public LocalAuthority getDesignatedLocalAuthority(CaseData caseData) {
        return caseData.getLocalAuthorities().stream()
            .map(Element::getValue)
            .filter(la -> YES.getValue().equals(la.getDesignated()))
            .findFirst()
            .orElseThrow();
    }

    public Optional<LocalAuthority> getSecondaryLocalAuthority(CaseData caseData) {
        return caseData.getLocalAuthorities().stream()
            .map(Element::getValue)
            .filter(la -> !YES.getValue().equals(la.getDesignated()))
            .findFirst();
    }

    public Optional<Element<LocalAuthority>> findLocalAuthority(CaseData caseData, String orgId) {

        return caseData.getLocalAuthorities().stream()
            .filter(la -> orgId.equals(la.getValue().getId()))
            .findFirst();
    }

    private Optional<String> getDesignatedOrgId(CaseData caseData) {
        return Optional.ofNullable(caseData.getLocalAuthorityPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .map(uk.gov.hmcts.reform.ccd.model.Organisation::getOrganisationID);
    }

    private Organisation getOrganisation(CaseData caseData) {
        final Organisation userOrganisation = organisationService.findOrganisation()
            .orElseThrow(() -> new OrganisationNotFound("Organisation not found for logged in user"));

        final Optional<String> outsourcingOrganisationId = Optional.ofNullable(caseData.getOutsourcingPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .map(uk.gov.hmcts.reform.ccd.model.Organisation::getOrganisationID);

        final boolean isUserFromOutsourcingOrganisation = outsourcingOrganisationId.isPresent()
            && outsourcingOrganisationId.get().equals(userOrganisation.getOrganisationIdentifier());

        if (isUserFromOutsourcingOrganisation) {
            if (nonNull(caseData.getLocalAuthorityPolicy())) {
                final String designatedLocalAuthorityId = caseData.getLocalAuthorityPolicy()
                    .getOrganisation()
                    .getOrganisationID();

                return organisationService.findOrganisation(designatedLocalAuthorityId)
                    .orElse(Organisation.builder()
                        .organisationIdentifier(designatedLocalAuthorityId)
                        .build());
            } else {
                return userOrganisation;
            }
        }

        return userOrganisation;
    }

    public boolean isOrgIdInPolicy(String orgId, OrganisationPolicy policy) {
        if (isNotEmpty(orgId) && isNotEmpty(policy) && isNotEmpty(policy.getOrganisation())) {
            return orgId.equals(policy.getOrganisation().getOrganisationID());
        }
        return false;
    }

    public boolean isApplicantOrOnBehalfOfOrgId(String orgId, CaseData caseData) {
        return isOrgIdInPolicy(orgId, caseData.getLocalAuthorityPolicy())
            || isOrgIdInPolicy(orgId, caseData.getOutsourcingPolicy())
            || isOrgIdInPolicy(orgId, caseData.getSharedLocalAuthorityPolicy());
    }
}
