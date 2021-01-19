package uk.gov.hmcts.reform.fpl.controllers.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.of;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;

@Api
@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReShareCaseController {

    private static final Set<String> ROLES_TO_BE_MIGRATED = Set.of(CaseRole.CREATOR.formattedName(),
        CaseRole.LASOLICITOR.formattedName());
    private final RequestData requestData;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseAccessDataStoreApi caseAccessDataStoreApi;
    private final ObjectMapper mapper;

    @PostMapping(value = "/support/cases/re-share", consumes = APPLICATION_JSON_VALUE)
    @Secured("caseworker-publiclaw-systemupdate") // NOSONAR
    public void shareCase() {

        final String query = new JSONObject()
            .put("from", 0)
            .put("size", 3000)
            .put("query", of("bool", of("must_not",
                List.of(
                    of("match", of("state", "Deleted")),
                    of("exists", of("field", "supplementary_data"))
                ))
            ))
            .toString();

        log.info("Access migration query: {}", query);

        final List<CaseDetails> casesDetails = coreCaseDataService.searchCases(CASE_TYPE, query);

        log.info("Access migration - number of cases to be migrated: {}", casesDetails.size());

        for (int i = 0; i < casesDetails.size(); i++) {
            CaseDetails caseDetail = casesDetails.get(i);
            try {
                log.info("Access migration - migrating access for case {}. ({}/{})",
                    caseDetail.getId(), i + 1, casesDetails.size());

                reShareCases(caseDetail);
            } catch (Exception e) {
                log.error("Access migration failed for case {}", caseDetail.getId(), e);
            }
        }

        log.info("Access migration finished");
    }

    private void reShareCases(CaseDetails caseDetails) throws InterruptedException {
        String caseId = caseDetails.getId().toString();
        final String serviceToken = authTokenGenerator.generate();

        OrganisationPolicy policy = mapper
            .convertValue(caseDetails.getData().get("localAuthorityPolicy"), OrganisationPolicy.class);

        if (policy == null) {
            log.warn("Access migration skipped for {} - missing organisation policy", caseId);
            return;
        }

        CaseAssignedUserRolesResource originalUserRoles = caseAccessDataStoreApi
            .getUserRoles(requestData.authorisation(), serviceToken, List.of(caseId));

        log.info("Access migration - Original access control: {}", originalUserRoles.toString());

        CaseAssignedUserRolesResource filtered = filterRoles(originalUserRoles);

        log.info("Access migration - Filtered access control: {}", filtered.toString());

        if (isEmpty(filtered.getCaseAssignedUserRoles())) {
            log.warn("Access migration skipped for {} - no roles to be migrate", caseId);
            return;
        }

        List<CaseAssignedUserRoleWithOrganisation> userRoles = filtered.getCaseAssignedUserRoles().stream()
            .map(user -> CaseAssignedUserRoleWithOrganisation.builder()
                .caseDataId(caseId)
                .userId(user.getUserId())
                .caseRole(user.getCaseRole())
                .organisationId(policy.getOrganisation().getOrganisationID())
                .build())
            .collect(Collectors.toList());

        AddCaseAssignedUserRolesRequest rolesToBeAdded = AddCaseAssignedUserRolesRequest.builder()
            .caseAssignedUserRoles(userRoles)
            .build();

        CaseAssignedUserRolesRequest rolesToBeRemoved = CaseAssignedUserRolesRequest.builder()
            .caseAssignedUserRoles(removeOrganisationId(userRoles))
            .build();

        caseAccessDataStoreApi.removeCaseUserRoles(requestData.authorisation(), serviceToken, rolesToBeRemoved);

        retry(caseId,
            () -> caseAccessDataStoreApi.addCaseUserRoles(requestData.authorisation(), serviceToken, rolesToBeAdded));
    }

    private void retry(String caseId, Runnable runnable) throws InterruptedException {
        int tryNumber = 0;
        int maxTries = 3;
        while (true) {
            try {
                runnable.run();
                break;
            } catch (Exception e) {
                tryNumber++;
                log.warn("Access migration - Grant roles try {} failed for {}", tryNumber, caseId);
                if (tryNumber >= maxTries) {
                    throw e;
                } else {
                    Thread.sleep(3000);
                }
            }
        }
    }

    private CaseAssignedUserRolesResource filterRoles(CaseAssignedUserRolesResource a) {
        return CaseAssignedUserRolesResource.builder()
            .caseAssignedUserRoles(a.getCaseAssignedUserRoles().stream()
                .filter(user -> ROLES_TO_BE_MIGRATED.contains(user.getCaseRole()))
                .collect(Collectors.toList()))
            .build();
    }

    private List<CaseAssignedUserRoleWithOrganisation> removeOrganisationId(
        List<CaseAssignedUserRoleWithOrganisation> userRoles) {
        return userRoles.stream()
            .map(caseAssignment -> caseAssignment.toBuilder().organisationId(null).build())
            .collect(Collectors.toList());
    }

}

