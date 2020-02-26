package uk.gov.hmcts.reform.rd.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.fpl.config.FeignConfiguration;
import uk.gov.hmcts.reform.rd.model.Organisation;
import uk.gov.hmcts.reform.rd.model.Status;
import uk.gov.hmcts.reform.rd.model.User;
import uk.gov.hmcts.reform.rd.model.Users;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(name = "rd-professional-api", url = "${rd_professional.api.url}", configuration = FeignConfiguration.class)
public interface OrganisationApi {
    @GetMapping("/refdata/external/v1/organisations/users")
    Users findUsersByOrganisation(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam(value = "status") Status status
    );

    @GetMapping("/refdata/external/v1/organisations/users/accountId")
    User findUserByEmail(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam(value = "email") final String email
    );

    @GetMapping("refdata/external/v1/organisations")
    Organisation findOrganisationById(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization
    );
}
