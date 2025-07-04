package uk.gov.hmcts.reform.rd.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.fpl.config.feign.FeignClientConfiguration;
import uk.gov.hmcts.reform.rd.model.PbaOrganisationResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;


@FeignClient(name = "pba-ref-data-api",
    url = "${pba.api.url}",
    configuration = FeignClientConfiguration.class)
public interface PbaApi {

    @GetMapping(value = "/refdata/external/v1/organisations/pbas")
    PbaOrganisationResponse retrievePbaNumbers(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestHeader("UserEmail") String email);
}
