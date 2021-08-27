package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.OutsourcingType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.LocalAuthorityName;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.OutsourcingType.EPS;
import static uk.gov.hmcts.reform.fpl.enums.OutsourcingType.MLA;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInitiationService {

    private final RequestData requestData;
    private final DynamicListService dynamicLists;
    private final CaseAccessService caseAccessService;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;
    private final LocalAuthorityService localAuthorities;
    private final HmctsCourtLookupConfiguration courtLookup;

    public Optional<String> getUserOrganisationId() {
        return organisationService.findOrganisation().map(Organisation::getOrganisationIdentifier);
    }

    public Optional<OutsourcingType> getOutsourcingType(String organisationId) {

        List<LocalAuthorityName> eps = localAuthorities.getOutsourcingLocalAuthorities(organisationId, EPS);
        List<LocalAuthorityName> mla = localAuthorities.getOutsourcingLocalAuthorities(organisationId, MLA);

        if (isNotEmpty(eps) && isNotEmpty(mla)) {
            throw new IllegalStateException(
                String.format("Organisation %s is configured as both EPS and MLA", organisationId));
        }

        if (isNotEmpty(mla)) {
            return Optional.of(MLA);
        }

        if (isNotEmpty(eps)) {
            return Optional.of(EPS);
        }
        return Optional.empty();
    }

    public DynamicList getOutsourcingLocalAuthorities(String orgId, OutsourcingType outsourcingType) {
        List<LocalAuthorityName> outsourcingLAs = localAuthorities
            .getOutsourcingLocalAuthorities(orgId, outsourcingType)
            .stream()
            .sorted(comparing(LocalAuthorityName::getName))
            .collect(Collectors.toList());

        Optional<LocalAuthorityName> userLocalAuthority = localAuthorities.getUserLocalAuthority();

        if (userLocalAuthority.isPresent()) {
            outsourcingLAs.add(0, userLocalAuthority.get());
            return dynamicLists.asDynamicList(
                outsourcingLAs,
                userLocalAuthority.get().getCode(),
                LocalAuthorityName::getCode,
                LocalAuthorityName::getName);
        } else {
            return dynamicLists.asDynamicList(
                outsourcingLAs,
                LocalAuthorityName::getCode,
                LocalAuthorityName::getName);
        }
    }

    public List<String> checkUserAllowedToCreateCase(CaseData caseData) {

        Optional<Organisation> userOrg = organisationService.findOrganisation();
        Optional<String> outsourcingLA = dynamicLists.getSelectedValue(caseData.getOutsourcingLAs());
        Optional<String> userLA = localAuthorities.getLocalAuthorityCode();
        Optional<String> caseLA = outsourcingLA.isPresent() ? outsourcingLA : userLA;

        boolean userInMO = userOrg.isPresent();
        boolean userInLA = userLA.isPresent();
        boolean caseOutsourced = outsourcingLA.isPresent() && !outsourcingLA.equals(userLA);

        if (userInMO && !userInLA && !caseOutsourced) {
            return List.of(
                "Email not recognised.",
                "Your email is not associated with a local authority or authorised legal firm.",
                "Email MyHMCTSsupport@justice.gov.uk for further guidance.");
        }

        if (!userInMO) {
            if (!userInLA && !caseOutsourced) {
                return List.of(
                    "Register for an account.",
                    "You cannot start an online application until you’re fully registered"
                        + " and have permission to start a case for a local authority.",
                    "Email MyHMCTSsupport@justice.gov.uk for further guidance."
                );
            }

            if (!featureToggleService.isCaseCreationForNotOnboardedUsersEnabled(caseLA.get())) {
                if (userInLA) {
                    return List.of(
                        "Register for an account.",
                        "You cannot start an online application until you’re fully registered.",
                        "Ask your local authority’s public law administrator, "
                            + "or email MyHMCTSsupport@justice.gov.uk, for help with registration.");
                }

                return List.of(
                    "Register for an account.",
                    "You cannot start an online application until you’re fully registered.",
                    "Email MyHMCTSsupport@justice.gov.uk for help with registration.");
            }
        }

        return emptyList();
    }

    public CaseData updateOrganisationsDetails(CaseData caseData) {
        Optional<Organisation> organisation = organisationService.findOrganisation();
        final String currentUserOrganisationId = organisation
            .map(Organisation::getOrganisationIdentifier)
            .orElse(null);
        final String currentUserOrganisationName = organisation
            .map(Organisation::getName)
            .orElse(null);

        final Optional<String> userLocalAuthority = localAuthorities.getLocalAuthorityCode();
        final Optional<String> outsourcingLocalAuthority = dynamicLists.getSelectedValue(caseData.getOutsourcingLAs());

        boolean isCaseOutsourced = outsourcingLocalAuthority.isPresent()
            && !userLocalAuthority.equals(outsourcingLocalAuthority);

        if (isCaseOutsourced) {
            String outsourcingOrgId = localAuthorities.getLocalAuthorityId(outsourcingLocalAuthority.get());
            String outsourcingOrgName = localAuthorities.getLocalAuthorityName(outsourcingLocalAuthority.get());

            CaseRole outsourcedOrganisationCaseRole = caseData.getOutsourcingType().getCaseRole();

            CaseData updatedCaseData = caseData.toBuilder()
                .outsourcingPolicy(organisationPolicy(currentUserOrganisationId, currentUserOrganisationName,
                    outsourcedOrganisationCaseRole))
                .localAuthorityPolicy(organisationPolicy(outsourcingOrgId, outsourcingOrgName, LASOLICITOR))
                .caseLocalAuthority(outsourcingLocalAuthority.get())
                .caseLocalAuthorityName(localAuthorities.getLocalAuthorityName(outsourcingLocalAuthority.get()))
                .build();

            return addCourtDetails(updatedCaseData);
        }

        if (userLocalAuthority.isPresent()) {
            CaseData updatedCaseData = caseData.toBuilder()
                .localAuthorityPolicy(
                    organisationPolicy(currentUserOrganisationId, currentUserOrganisationName, LASOLICITOR))
                .caseLocalAuthority(userLocalAuthority.get())
                .caseLocalAuthorityName(localAuthorities.getLocalAuthorityName(userLocalAuthority.get()))
                .build();

            return addCourtDetails(updatedCaseData);
        }

        throw new IllegalStateException("Cannot determine local authority for a case");
    }

    private CaseData addCourtDetails(CaseData caseData) {
        final List<Court> courts = courtLookup.getCourts(caseData.getCaseLocalAuthority());

        if (isNotEmpty(courts)) {
            if (courts.size() == 1) {
                return caseData.toBuilder().court(courts.get(0)).build();
            } else {
                return caseData.toBuilder().multiCourts(YesNo.YES).build();
            }
        }

        return caseData;
    }

    public void grantCaseAccess(CaseData caseData) {
        final Long caseId = caseData.getId();
        final String creatorId = requestData.userId();
        final String localAuthority = caseData.getCaseLocalAuthority();

        caseAccessService.revokeCaseRoleFromUser(caseId, creatorId, CREATOR);

        if (nonNull(caseData.getOutsourcingPolicy())) {
            final CaseRole caseRole = getCaseRole(caseData.getOutsourcingPolicy());
            caseAccessService.grantCaseRoleToUser(caseId, creatorId, caseRole);
        } else {
            final CaseRole caseRole = getCaseRole(caseData.getLocalAuthorityPolicy());
            caseAccessService.grantCaseRoleToLocalAuthority(caseId, creatorId, localAuthority, caseRole);
        }
    }

    private CaseRole getCaseRole(OrganisationPolicy organisationPolicy) {
        return CaseRole.from(organisationPolicy.getOrgPolicyCaseAssignedRole());
    }
}
