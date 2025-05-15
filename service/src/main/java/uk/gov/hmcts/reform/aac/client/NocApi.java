package uk.gov.hmcts.reform.aac.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.aac.model.DecisionRequest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.config.feign.FeignClientConfiguration;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "aac-api",
    url = "${case-assignment.api.url}",
    configuration = FeignClientConfiguration.class
)
public interface NocApi {

    @PostMapping(
        value = "/noc/apply-decision",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    AboutToStartOrSubmitCallbackResponse applyDecision(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody DecisionRequest decisionRequest);
}
