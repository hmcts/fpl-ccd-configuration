package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.EPSMANAGING;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInitiationService {

    private final RequestData requestData;
    private final DynamicListService dynamicLists;
    private final CaseAccessService caseAccessService;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;
    private final LocalAuthorityService localAuthorityService;

    public Optional<DynamicList> getOutsourcingLocalAuthoritiesDynamicList() {
        final List<LocalAuthority> outsourcingLAs = getOutsourcingLocalAuthorities()
            .stream()
            .sorted(comparing(LocalAuthority::getName))
            .collect(Collectors.toList());

        if (isEmpty(outsourcingLAs)) {
            return empty();
        }
        return ofNullable(dynamicLists.asDynamicList(outsourcingLAs, LocalAuthority::getCode, LocalAuthority::getName));
    }

    public List<String> checkUserAllowedToCreateCase(CaseData caseData) {
        final List<String> errors = new ArrayList<>();

        final String localAuthorityCode = getLocalAuthorityCode(caseData);

        if (!featureToggleService.isCaseCreationForNotOnboardedUsersEnabled(localAuthorityCode)
            && organisationService.findOrganisation().isEmpty()) {
            errors.add("Register for an account.");
            errors.add("You cannot start an online application until you’re fully registered.");
            errors.add("Ask your local authority’s public law administrator, or email MyHMCTSsupport@justice.gov.uk, "
                + "for help with registration.");
        }

        return errors;
    }

    public CaseData updateOrganisationsDetails(CaseData caseData) {
        final String currentUserOrganisationId = organisationService.findOrganisation()
            .map(Organisation::getOrganisationIdentifier)
            .orElse(null);

        final Optional<String> outsourcingLA = dynamicLists.getSelectedValue(caseData.getOutsourcingLAs());

        if (outsourcingLA.isPresent()) {
            String outsourcingOrgId = localAuthorityService.getLocalAuthorityId(outsourcingLA.get());

            return caseData.toBuilder()
                .outsourcingPolicy(organisationPolicy(currentUserOrganisationId, EPSMANAGING))
                .localAuthorityPolicy(organisationPolicy(outsourcingOrgId, LASOLICITOR))
                .caseLocalAuthority(outsourcingLA.get())
                .caseLocalAuthorityName(localAuthorityService.getLocalAuthorityName(outsourcingLA.get()))
                .build();
        } else {
            return caseData.toBuilder()
                .localAuthorityPolicy(organisationPolicy(currentUserOrganisationId, LASOLICITOR))
                .caseLocalAuthority(localAuthorityService.getLocalAuthorityCode())
                .caseLocalAuthorityName(localAuthorityService
                    .getLocalAuthorityName(localAuthorityService.getLocalAuthorityCode()))
                .build();
        }
    }

    public void grantCaseAccess(CaseData caseData) {
        final Long caseId = caseData.getId();

        if (nonNull(caseData.getOutsourcingPolicy())) {
            caseAccessService.revokeCaseRoleFromUser(caseId, requestData.userId(), CREATOR);
            caseAccessService.grantCaseRoleToUser(caseId, requestData.userId(), EPSMANAGING);
        } else {
            caseAccessService.grantCaseRoleToLocalAuthority(caseId, caseData.getCaseLocalAuthority(), LASOLICITOR);
        }
    }

    private String getLocalAuthorityCode(CaseData caseData) {
        return dynamicLists.getSelectedValue(caseData.getOutsourcingLAs())
            .orElseGet(localAuthorityService::getLocalAuthorityCode);
    }

    private List<LocalAuthority> getOutsourcingLocalAuthorities() {
        return organisationService.findOrganisation()
            .map(Organisation::getOrganisationIdentifier)
            .map(localAuthorityService::getOutsourcingLocalAuthorities)
            .orElse(emptyList());
    }

}
