package uk.gov.hmcts.reform.fpl.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.AsyncConfiguration;

import javax.servlet.http.HttpServletRequest;

/**
 Access http request related attributes.
 Uses ThreadLocalCache as a cache to avoid accessing http request from spawned threads
 (http request can be already closed at the time when new thread access it)
 @see AsyncConfiguration.AsyncTaskDecorator
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
}
