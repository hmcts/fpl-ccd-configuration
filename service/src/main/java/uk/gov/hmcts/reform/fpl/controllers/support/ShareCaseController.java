package uk.gov.hmcts.reform.fpl.controllers.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.util.List;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;

@Api
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareCaseController {
    private static final Set<String> CASE_ROLES = Set.of(CREATOR.formattedName(), LASOLICITOR.formattedName());
    private final RequestData requestData;
    private final CaseUserApi caseUser;
    private final AuthTokenGenerator authTokenGenerator;

    @PostMapping(value = "/support/case/{caseId}/share", consumes = APPLICATION_JSON_VALUE)
    @Secured("caseworker-publiclaw-systemupdate")
    public void shareCase(@PathVariable("caseId") String caseId, @RequestBody Users users) {

        final String serviceToken = authTokenGenerator.generate();

        users.ids.forEach(
            userId -> caseUser.updateCaseRolesForUser(
                requestData.authorisation(),
                serviceToken,
                caseId,
                userId,
                new CaseUser(userId, CASE_ROLES)));
    }

    static class Users {
        @JsonProperty
        List<String> ids;
    }
}

