package uk.gov.hmcts.reform.ccd.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.AuditEventsResponse;
import uk.gov.hmcts.reform.fpl.config.feign.FeignClientConfiguration;

import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(
    name = "core-case-data-api-v2",
    url = "${core_case_data.api.url}",
    configuration = FeignClientConfiguration.class
)
public interface CoreCaseDataApiV2 {
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String EXPERIMENTAL = "experimental=true";

    @PostMapping(
        path = "/case-types/{caseTypeId}/cases",
        headers = EXPERIMENTAL
    )
    Map saveCase(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable("caseTypeId") String caseTypeId,
        @RequestBody CaseDataContent content);

    @GetMapping("/cases/{caseId}/events")
    AuditEventsResponse getAuditEvents(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader("experimental") boolean experimental,
        @PathVariable("caseId") String caseId
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/cases/{caseId}/supplementary-data"
    )
    CaseDetails submitSupplementaryData(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("caseId") String caseId,
        @RequestBody Map<String, Map<String, Map<String, Object>>> supplementaryData
    );
}
