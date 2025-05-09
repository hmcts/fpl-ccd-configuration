package uk.gov.hmcts.reform.rd.client;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.fpl.config.feign.FeignClientConfiguration;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;
import uk.gov.hmcts.reform.rd.model.JudicialUserRequest;

import java.util.List;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.fpl.config.CacheConfiguration.JUDICIAL_USER_CACHE;
import static uk.gov.hmcts.reform.fpl.config.CacheConfiguration.REQUEST_SCOPED_CACHE_MANAGER;

@FeignClient(
    name = "rd-judicial-api",
    url = "${rd_judicial.api.url}",
    configuration = FeignClientConfiguration.class
)
public interface JudicialApi {


    @Cacheable(cacheManager = REQUEST_SCOPED_CACHE_MANAGER, cacheNames = JUDICIAL_USER_CACHE)
    @PostMapping("/refdata/judicial/users")
    List<JudicialUserProfile> findUsers(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader("page_size") int pageSize,
        @RequestHeader(value = ACCEPT, required = false) String accept,
        @RequestBody JudicialUserRequest request
    );

}
