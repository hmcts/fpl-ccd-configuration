package uk.gov.hmcts.reform.am.client;


import jdk.jfr.ContentType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.am.model.AssignmentRequest;
import uk.gov.hmcts.reform.am.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.fpl.config.feign.FeignClientConfiguration;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "am-role-assignment-api",
    url = "${am_role_assignment.api.url}",
    configuration = FeignClientConfiguration.class
)
public interface AmApi {

    @PostMapping(
        value = "/am/role-assignments",
        consumes = APPLICATION_JSON_VALUE,
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    RoleAssignmentRequestResource createRoleAssignment(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody AssignmentRequest request
    );

}
