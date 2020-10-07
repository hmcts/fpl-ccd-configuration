package uk.gov.hmcts.reform.fpl.controllers.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import static java.util.Map.of;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;

@Api
@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareCaseController {
    private static final Set<String> CASE_ROLES = Set.of(CREATOR.formattedName(), LASOLICITOR.formattedName());
    private final RequestData requestData;
    private final CaseUserApi caseUser;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataService coreCaseDataService;

    @PostMapping(value = "/support/case/{caseId}/share", consumes = APPLICATION_JSON_VALUE)
    @Secured("caseworker-publiclaw-systemupdate")
    public void shareCase(@PathVariable("caseId") String caseId, @RequestBody Users users) {
        shareCases(users.ids, List.of(caseId));
    }

    @PostMapping(value = "/support/cases/share", consumes = APPLICATION_JSON_VALUE)
    @Secured("caseworker-publiclaw-systemupdate")
    public void shareCases(@Valid @RequestBody ShareLocalAuthorityCases shareCases) {

        final String query = new JSONObject()
            .put("from", 0)
            .put("size", 1000)
            .put("query", of("match", of("data.caseLocalAuthority", shareCases.localAuthority)))
            .toString();

        final List<String> caseIds = coreCaseDataService.searchCases(CASE_TYPE, query).stream()
            //`Extra precaution as ES match does not guarantee strict equality
            .filter(caseDetails -> shareCases.localAuthority.equals(caseDetails.getData().get("caseLocalAuthority")))
            .map(CaseDetails::getId)
            .map(Object::toString)
            .collect(Collectors.toList());

        log.info("Share case. Found {} case(s) for '{}' LA", caseIds.size(), shareCases.localAuthority);

        shareCases(shareCases.usersIds, caseIds);
    }

    private void shareCases(List<String> usersIds, List<String> casesIds) {
        final String serviceToken = authTokenGenerator.generate();

        usersIds.forEach(userId ->
            casesIds.forEach(caseId -> {
                caseUser.updateCaseRolesForUser(
                    requestData.authorisation(),
                    serviceToken,
                    caseId,
                    userId,
                    new CaseUser(userId, CASE_ROLES));
                log.info("Share case. User {} granted access to {}", userId, caseId);
            })
        );
    }

    static class Users {
        @JsonProperty
        List<String> ids;
    }

    static class ShareLocalAuthorityCases {
        @NotEmpty(message = "Local authority must be provided")
        @JsonProperty
        String localAuthority;

        @NotEmpty(message = "List of users ids must be provided")
        @JsonProperty
        List<String> usersIds;
    }
}

