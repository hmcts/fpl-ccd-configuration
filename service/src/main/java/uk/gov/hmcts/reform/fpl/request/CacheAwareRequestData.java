package uk.gov.hmcts.reform.fpl.request;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.AsyncConfiguration;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Access http request related attributes.
 * Uses ThreadLocalCache as a cache to avoid accessing http request from spawned threads
 * (http request can be already closed at the time when new thread access it)
 *
 * @see AsyncConfiguration.AsyncTaskDecorator
 */

@Primary
@Service
public class CacheAwareRequestData implements RequestData {

    private final HttpServletRequest httpServletRequest;

    @Autowired
    public CacheAwareRequestData(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public String authorisation() {
        return RequestDataCache.get()
            .map(RequestData::authorisation)
            .orElseGet(() -> httpServletRequest.getHeader("authorization"));
    }

    public String userId() {
        return RequestDataCache.get()
            .map(RequestData::userId)
            .orElseGet(() -> httpServletRequest.getHeader("user-id"));
    }

    public Set<String> userRoles() {
        return RequestDataCache.get()
            .map(RequestData::userRoles)
            .orElseGet(() -> extractUserRoles(httpServletRequest));
    }

    private Set<String> extractUserRoles(HttpServletRequest httpServletRequest) {
        String userRoles = httpServletRequest.getHeader("user-roles");

        if (isBlank(userRoles)) {
            return emptySet();
        }

        return Stream.of(userRoles.split(","))
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());
    }
}
