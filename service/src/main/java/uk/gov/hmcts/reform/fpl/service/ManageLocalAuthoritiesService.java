package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityIdLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.CaseTransferred;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityAdded;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityRemoved;
import uk.gov.hmcts.reform.fpl.exceptions.OrganisationPolicyNotFound;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthoritiesEventData;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.ADD;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.REMOVE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageLocalAuthoritiesService {

    private final Time time;
    private final CourtService courtService;
    private final ValidateEmailService emailService;
    private final DynamicListService dynamicListService;
    private final OrganisationService organisationService;
    private final HmctsCourtLookupConfiguration courtLookup;
    private final LocalAuthorityIdLookupConfiguration localAuthorityIds;
    private final LocalAuthorityNameLookupConfiguration localAuthorities;
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmails;
    private final ApplicantLocalAuthorityService applicantLocalAuthorityService;

    public String getCurrentCourtName(CaseData caseData) {
        return courtService.getCourtName(caseData);
    }

    public DynamicList getCourtsToTransfer(CaseData caseData) {

        final Court currentCaseCourt = courtService.getCourt(caseData);

        final String candidateLACode = getLocalAuthorityToTransferCode(caseData);

        final List<Court> candidateLACourts = courtLookup.getCourts(candidateLACode);
        final List<Court> designatedLACourts = courtLookup.getCourts(caseData.getCaseLocalAuthority());

        final List<Pair<String, String>> courts = concat(designatedLACourts.stream(), candidateLACourts.stream())
            .filter(court -> notEqual(court.getCode(), currentCaseCourt.getCode()))
            .distinct()
            .sorted(comparing(Court::getName))
            .map(court -> Pair.of(court.getCode(), court.getName()))
            .collect(toList());

        return dynamicListService.asDynamicList(courts);
    }

    public List<String> validateAction(CaseData caseData) {

        boolean isCaseShared = getSharedOrganisation(caseData).isPresent();

        final LocalAuthorityAction action = caseData.getLocalAuthoritiesEventData().getLocalAuthorityAction();

        if (ADD == action && isCaseShared) {
            return List.of("Case access has already been given to local authority. Remove their access to continue.");
        }

        if (REMOVE == action && !isCaseShared) {
            return List.of("There are no other local authorities to remove from this case");
        }

        return emptyList();
    }

    public List<String> validateLocalAuthorityToShare(LocalAuthoritiesEventData eventData) {

        final List<String> errors = new ArrayList<>();

        emailService.validate(eventData.getLocalAuthorityEmail()).ifPresent(errors::add);

        return errors;
    }

    public List<String> validateLocalAuthorityToTransfer(LocalAuthoritiesEventData eventData) {

        final List<String> errors = new ArrayList<>();

        ofNullable(eventData.getLocalAuthorityToTransfer())
            .map(LocalAuthority::getEmail)
            .flatMap(email -> emailService.validate(email,
                "Enter local authority's group email address in the correct format, for example name@example.com"))
            .ifPresent(errors::add);

        ofNullable(eventData.getLocalAuthorityToTransferSolicitor())
            .map(Colleague::getEmail)
            .flatMap(email -> emailService.validate(email,
                "Enter local authority solicitor's email address in the correct format, for example name@example.com"))
            .ifPresent(errors::add);

        return errors;
    }

    public DynamicList getLocalAuthoritiesToShare(CaseData caseData) {

        final Map<String, String> sortedLAs = new TreeMap<>(this.localAuthorities.getLocalAuthoritiesNames());

        ofNullable(caseData.getCaseLocalAuthority()).ifPresent(sortedLAs::remove);

        return dynamicListService.asDynamicList(sortedLAs);
    }

    public DynamicList getLocalAuthoritiesToTransfer(CaseData caseData) {

        final Map<String, String> sortedLAs = new TreeMap<>(this.localAuthorities.getLocalAuthoritiesNames());

        ofNullable(caseData.getCaseLocalAuthority()).ifPresent(sortedLAs::remove);
        ofNullable(getSharedLocalAuthorityCode(caseData)).ifPresent(sortedLAs::remove);

        return dynamicListService.asDynamicList(sortedLAs);
    }

    public String getSelectedLocalAuthorityEmail(LocalAuthoritiesEventData eventData) {

        return ofNullable(eventData.getLocalAuthoritiesToShare())
            .map(DynamicList::getValueCode)
            .flatMap(localAuthorityEmails::getSharedInbox)
            .orElse(null);
    }

    public String getSharedLocalAuthorityName(CaseData caseData) {

        return getSharedOrganisation(caseData)
            .map(Organisation::getOrganisationName)
            .orElse(null);
    }

    public List<Element<LocalAuthority>> removeSharedLocalAuthority(CaseData caseData) {

        getSharedOrganisation(caseData)
            .map(Organisation::getOrganisationID)
            .ifPresent(orgId ->
                caseData.getLocalAuthorities().removeIf(la -> Objects.equals(la.getValue().getId(), orgId)));

        return caseData.getLocalAuthorities();
    }

    public List<Element<LocalAuthority>> addSharedLocalAuthority(CaseData caseData) {

        final LocalAuthority localAuthorityToAdd = ofNullable(caseData.getLocalAuthoritiesEventData())
            .map(LocalAuthoritiesEventData::getLocalAuthoritiesToShare)
            .map(DynamicList::getValueCode)
            .map(localAuthorityIds::getLocalAuthorityId)
            .map(organisationService::getOrganisation)
            .map(applicantLocalAuthorityService::getLocalAuthority)
            .orElseThrow();

        localAuthorityToAdd.setEmail(caseData.getLocalAuthoritiesEventData().getLocalAuthorityEmail());
        localAuthorityToAdd.setDesignated(NO.getValue());

        caseData.getLocalAuthorities().add(element(localAuthorityToAdd));

        return caseData.getLocalAuthorities();
    }

    public ChangeOrganisationRequest getOrgRemovalRequest(CaseData caseData) {

        final OrganisationPolicy organisationPolicy = ofNullable(caseData.getSharedLocalAuthorityPolicy()).orElseThrow(
            () -> new OrganisationPolicyNotFound("SharedLocalAuthorityPolicy not found for case " + caseData.getId()));

        final DynamicListElement roleItem = DynamicListElement.builder()
            .code(organisationPolicy.getOrgPolicyCaseAssignedRole())
            .label(organisationPolicy.getOrgPolicyCaseAssignedRole())
            .build();

        return ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .requestTimestamp(time.now())
            .caseRoleId(DynamicList.builder()
                .value(roleItem)
                .listItems(List.of(roleItem))
                .build())
            .organisationToRemove(organisationPolicy.getOrganisation())
            .build();
    }

    public OrganisationPolicy getSharedLocalAuthorityPolicy(CaseData caseData) {

        final DynamicListElement selectedLocalAuthority = ofNullable(caseData.getLocalAuthoritiesEventData())
            .map(LocalAuthoritiesEventData::getLocalAuthoritiesToShare)
            .map(DynamicList::getValue)
            .filter(selectedItem -> nonNull(selectedItem.getCode()))
            .orElseThrow();

        final String selectedLocalAuthorityId = localAuthorityIds.getLocalAuthorityId(selectedLocalAuthority.getCode());

        return OrganisationPolicy.builder()
            .organisation(Organisation.builder()
                .organisationID(selectedLocalAuthorityId)
                .organisationName(selectedLocalAuthority.getLabel())
                .build())
            .orgPolicyCaseAssignedRole(LASHARED.formattedName())
            .build();
    }

    public List<Object> getChangeEvent(CaseData caseData, CaseData caseDataBefore) {

        if (!Objects.equals(getDesignatedOrganisationId(caseData), getDesignatedOrganisationId(caseDataBefore))) {
            return List.of(
                new CaseTransferred(caseData, caseDataBefore),
                new AfterSubmissionCaseDataUpdated(caseData, caseDataBefore));
        }

        if (getSharedOrganisation(caseData).isPresent() && getSharedOrganisation(caseDataBefore).isEmpty()) {
            return List.of(new SecondaryLocalAuthorityAdded(caseData));
        }

        if (getSharedOrganisation(caseData).isEmpty() && getSharedOrganisation(caseDataBefore).isPresent()) {
            return List.of(new SecondaryLocalAuthorityRemoved(caseData, caseDataBefore));
        }

        return emptyList();
    }

    public LocalAuthority getLocalAuthorityToTransferDetails(CaseData caseData) {

        final String localAuthorityCode = getLocalAuthorityToTransferCode(caseData);
        final String localAuthorityOrgId = localAuthorityIds.getLocalAuthorityId(localAuthorityCode);

        final LocalAuthority localAuthority = applicantLocalAuthorityService
            .findLocalAuthority(caseData, localAuthorityOrgId)
            .map(Element::getValue)
            .orElseGet(() -> {
                final uk.gov.hmcts.reform.rd.model.Organisation organisation = organisationService
                    .getOrganisation(localAuthorityOrgId);
                final LocalAuthority newLocalAuthority = applicantLocalAuthorityService.getLocalAuthority(organisation);

                newLocalAuthority.setEmail(localAuthorityEmails.getSharedInbox(localAuthorityCode).orElse(null));

                return newLocalAuthority;
            });

        final Colleague solicitor = localAuthority.getFirstSolicitor()
            .orElse(Colleague.builder().role(SOLICITOR).notificationRecipient(YES.getValue()).build());

        localAuthority.setColleagues(wrapElements(solicitor));

        return localAuthority;
    }

    public Organisation transfer(CaseData caseData) {

        final LocalAuthoritiesEventData eventData = caseData.getLocalAuthoritiesEventData();

        final LocalAuthority newDesignatedLocalAuthority = eventData.getLocalAuthorityToTransfer().toBuilder()
            .designated(YES.getValue())
            .build();

        final Colleague newDesignatedLocalAuthoritySolicitor = eventData.getLocalAuthorityToTransferSolicitor();

        caseData.getLocalAuthorities().removeIf(la -> YES.getValue().equals(la.getValue().getDesignated()));

        final Optional<LocalAuthority> existingLocalAuthority = applicantLocalAuthorityService
            .findLocalAuthority(caseData, newDesignatedLocalAuthority.getId())
            .map(Element::getValue);

        if (existingLocalAuthority.isPresent()) {
            final LocalAuthority old = existingLocalAuthority.get();

            old.setName(newDesignatedLocalAuthority.getName());
            old.setEmail(newDesignatedLocalAuthority.getEmail());
            old.setAddress(newDesignatedLocalAuthority.getAddress());
            old.setDesignated(newDesignatedLocalAuthority.getDesignated());

            final Optional<Colleague> oldSolicitor = old.getFirstSolicitor();

            if (oldSolicitor.isPresent()) {
                oldSolicitor.get().setFullName(newDesignatedLocalAuthoritySolicitor.getFullName());
                oldSolicitor.get().setEmail(newDesignatedLocalAuthoritySolicitor.getEmail());
            } else {
                final List<Element<Colleague>> colleagues = defaultIfNull(old.getColleagues(), new ArrayList<>());
                if (isEmpty(colleagues)) {
                    newDesignatedLocalAuthoritySolicitor.setMainContact(YES.getValue());
                }

                colleagues.add(element(null, newDesignatedLocalAuthoritySolicitor));
            }
        } else {
            newDesignatedLocalAuthoritySolicitor.setMainContact(YES.getValue());
            newDesignatedLocalAuthority.setColleagues(wrapElements(newDesignatedLocalAuthoritySolicitor));
            caseData.getLocalAuthorities().add(0, element(newDesignatedLocalAuthority));
        }

        ofNullable(eventData.getCourtsToTransfer())
            .map(DynamicList::getValueCode)
            .flatMap(courtLookup::getCourtByCode)
            .ifPresent(caseData::setCourt);

        caseData.setCaseLocalAuthority(localAuthorityIds.getLocalAuthorityCode(newDesignatedLocalAuthority.getId())
            .orElseThrow());

        caseData.setCaseLocalAuthorityName(newDesignatedLocalAuthority.getName());

        return Organisation.builder()
            .organisationID(newDesignatedLocalAuthority.getId())
            .organisationName(newDesignatedLocalAuthority.getName())
            .build();
    }

    public boolean isSecondary(CaseData caseData, Organisation organisation) {
        final String orgId = Optional.ofNullable(organisation).map(Organisation::getOrganisationID).orElse(null);
        final String sharedId = Optional.ofNullable(caseData.getSharedLocalAuthorityPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .orElse(null);

        return nonNull(orgId) && orgId.equals(sharedId);
    }

    private Optional<Organisation> getSharedOrganisation(CaseData caseData) {

        return Optional.ofNullable(caseData.getSharedLocalAuthorityPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .filter(org -> nonNull(org.getOrganisationID()));
    }

    private String getDesignatedOrganisationId(CaseData caseData) {
        return caseData.getLocalAuthorityPolicy().getOrganisation().getOrganisationID();
    }

    private boolean isCaseShared(CaseData caseData) {
        return getSharedOrganisation(caseData).isPresent();
    }

    private String getSharedLocalAuthorityCode(CaseData caseData) {

        return getSharedOrganisation(caseData)
            .map(Organisation::getOrganisationID)
            .flatMap(localAuthorityIds::getLocalAuthorityCode)
            .orElse(null);
    }

    private String getLocalAuthorityToTransferCode(CaseData caseData) {

        final LocalAuthoritiesEventData eventData = caseData.getLocalAuthoritiesEventData();

        boolean isCaseShared = isCaseShared(caseData);

        if (isCaseShared) {
            if (YES.equals(eventData.getTransferToSharedLocalAuthority())) {
                return ofNullable(caseData.getSharedLocalAuthorityPolicy())
                    .map(OrganisationPolicy::getOrganisation)
                    .map(Organisation::getOrganisationID)
                    .flatMap(localAuthorityIds::getLocalAuthorityCode)
                    .orElseThrow();
            }
            return eventData.getLocalAuthoritiesToTransferWithoutShared().getValueCode();
        }

        return eventData.getLocalAuthoritiesToTransfer().getValueCode();
    }

}
