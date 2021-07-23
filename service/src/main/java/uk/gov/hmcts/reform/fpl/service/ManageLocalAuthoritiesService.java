package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityIdLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityAdded;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityRemoved;
import uk.gov.hmcts.reform.fpl.exceptions.OrganisationPolicyNotFound;
import uk.gov.hmcts.reform.fpl.model.CaseData;
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
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.ADD;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.REMOVE;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.TRANSFER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageLocalAuthoritiesService {

    private final Time time;
    private final ValidateEmailService emailService;
    private final DynamicListService dynamicListService;
    private final OrganisationService organisationService;
    private final LocalAuthorityIdLookupConfiguration localAuthorityId;
    private final LocalAuthorityNameLookupConfiguration localAuthorities;
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmails;
    private final ApplicantLocalAuthorityService applicantLocalAuthorityService;

    public List<String> validateAction(CaseData caseData) {

        boolean isCaseShared = getShareOrganisation(caseData).isPresent();

        final LocalAuthorityAction action = caseData.getLocalAuthoritiesEventData().getLocalAuthorityAction();

        if (ADD.equals(action) && isCaseShared) {
            return List.of("Case access has already been given to local authority. Remove their access to continue");
        }

        if (REMOVE.equals(action) && !isCaseShared) {
            return List.of("There are no other local authorities to remove from this case");
        }

        if (TRANSFER.equals(action)) {
            return List.of("Transfer of case is not supported yet");
        }

        return emptyList();
    }

    public List<String> validateLocalAuthorityEmail(LocalAuthoritiesEventData eventData) {

        final List<String> errors = new ArrayList<>();

        emailService.validate(eventData.getLocalAuthorityEmail()).ifPresent(errors::add);

        return errors;
    }

    public DynamicList getLocalAuthoritiesToShare(CaseData caseData) {

        final Map<String, String> sortedLAs = new TreeMap<>(this.localAuthorities.getLocalAuthoritiesNames());

        ofNullable(caseData.getCaseLocalAuthority()).ifPresent(sortedLAs::remove);

        return dynamicListService.asDynamicList(sortedLAs);
    }

    public String getSelectedLocalAuthorityEmail(LocalAuthoritiesEventData eventData) {
        return ofNullable(eventData.getLocalAuthoritiesToShare())
            .map(DynamicList::getValueCode)
            .flatMap(localAuthorityEmails::getLocalAuthority)
            .orElse(null);
    }

    public String getSharedLocalAuthorityName(CaseData caseData) {

        return getShareOrganisation(caseData)
            .map(Organisation::getOrganisationName)
            .orElse(null);
    }

    public List<Element<LocalAuthority>> removeSharedLocalAuthority(CaseData caseData) {

        getShareOrganisation(caseData)
            .map(Organisation::getOrganisationID)
            .ifPresent(orgId ->
                caseData.getLocalAuthorities().removeIf(la -> Objects.equals(la.getValue().getId(), orgId)));

        return caseData.getLocalAuthorities();
    }

    public List<Element<LocalAuthority>> addSharedLocalAuthority(CaseData caseData) {

        final LocalAuthority localAuthorityToAdd = ofNullable(caseData.getLocalAuthoritiesEventData())
            .map(LocalAuthoritiesEventData::getLocalAuthoritiesToShare)
            .map(DynamicList::getValueCode)
            .map(localAuthorityId::getLocalAuthorityId)
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
            .orElseThrow();

        final String selectedLocalAuthorityId = localAuthorityId.getLocalAuthorityId(selectedLocalAuthority.getCode());

        return OrganisationPolicy.builder()
            .organisation(Organisation.builder()
                .organisationID(selectedLocalAuthorityId)
                .organisationName(selectedLocalAuthority.getLabel())
                .build())
            .orgPolicyCaseAssignedRole(LASHARED.formattedName())
            .build();
    }

    public Optional<Object> getChangeEvent(CaseData caseData, CaseData caseDataBefore) {

        if (getShareOrganisation(caseData).isPresent() && getShareOrganisation(caseDataBefore).isEmpty()) {
            return Optional.of(new SecondaryLocalAuthorityAdded(caseData));
        }

        if (getShareOrganisation(caseData).isEmpty() && getShareOrganisation(caseDataBefore).isPresent()) {
            return Optional.of(new SecondaryLocalAuthorityRemoved(caseData, caseDataBefore));
        }

        return Optional.empty();
    }

    private Optional<Organisation> getShareOrganisation(CaseData caseData) {

        return Optional.ofNullable(caseData.getSharedLocalAuthorityPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .filter(org -> nonNull(org.getOrganisationID()));
    }
}
