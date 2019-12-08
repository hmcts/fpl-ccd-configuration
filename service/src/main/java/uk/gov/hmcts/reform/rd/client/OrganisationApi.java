package uk.gov.hmcts.reform.rd.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "rd-professional-api", url = "${rd_professional.api.url}")
public interface OrganisationApi {
}
