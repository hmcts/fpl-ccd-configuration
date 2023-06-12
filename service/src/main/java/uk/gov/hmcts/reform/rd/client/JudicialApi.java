package uk.gov.hmcts.reform.rd.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.fpl.config.feign.FeignClientConfiguration;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;
import uk.gov.hmcts.reform.rd.model.JudicialUserRequest;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "rd-judicial-api",
    url = "${rd_judicial.api.url}",
    configuration = FeignClientConfiguration.class
)
public interface JudicialApi {

    @GetMapping("/refdata/judicial/users")
    List<JudicialUserProfile> findUsersInCurrentUserOrganisation(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody JudicialUserRequest request
    );

}
