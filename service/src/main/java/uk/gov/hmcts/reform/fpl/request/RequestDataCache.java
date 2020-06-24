package uk.gov.hmcts.reform.fpl.request;

import java.util.Optional;

public class RequestDataCache {
    private static final ThreadLocal<SimpleRequestData> REQUEST_DATA_THREAD_LOCAL = new ThreadLocal<>();

    private RequestDataCache() {
    }

    public static void add(SimpleRequestData requestData) {
        REQUEST_DATA_THREAD_LOCAL.set(new SimpleRequestData(requestData));
    }

    public static void remove() {
        REQUEST_DATA_THREAD_LOCAL.remove();
    }

    public static Optional<RequestData> get() {
        return Optional.ofNullable(REQUEST_DATA_THREAD_LOCAL.get());
    }
}
