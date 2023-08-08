package uk.gov.hmcts.reform.rd.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.fpl.config.feign.FeignClientConfiguration;
import uk.gov.hmcts.reform.rd.model.StaffProfile;

import java.util.List;

@FeignClient(
    name = "rd-staff-api",
    url = "${rd_staff.api.url}",
    configuration = FeignClientConfiguration.class
)
public interface StaffApi {

    @GetMapping(value = "refdata/case-worker/profile/search", consumes = "application/json")
    List<StaffProfile> getAllStaffResponseDetails(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("page_size") int pageSize,
        @RequestParam("serviceCode") String serviceCode,
        @RequestParam("jobTitle") String jobTitle
    );


}
