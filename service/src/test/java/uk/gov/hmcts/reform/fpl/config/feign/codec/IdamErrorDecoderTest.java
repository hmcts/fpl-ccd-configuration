package uk.gov.hmcts.reform.fpl.config.feign.codec;

import feign.Request;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IdamErrorDecoderTest {

    private static final Request REQUEST = Request.create(GET, EMPTY, Map.of(), new byte[] {}, UTF_8, null);
    private static final Map<String, Collection<String>> EMPTY_HEADERS = Map.of();
    private static final String EMPTY_REASON = "";
    private static final byte[] EMPTY_BODY = new byte[0];

    private static final int STATUS_5XX = 500;
    private static final int STATUS_NOT_5XX = 404;

    private static final Exception NON_RETRYABLE_EXCEPTION = new Exception();
    private static final Exception RETRYABLE_EXCEPTION = new RetryableException(500, "", null, 0L, REQUEST);

    private static final String EMPTY_METHOD_KEY = "";

    private ErrorDecoder defaultDecoder;
    private IdamErrorDecoder idamErrorDecoder;

    @BeforeEach
    void setUp() {
        defaultDecoder = mock(ErrorDecoder.class);
        idamErrorDecoder = new IdamErrorDecoder(defaultDecoder);
    }

    @Test
    void shouldReturnExistingRetryableError() {
        Response response = response(STATUS_NOT_5XX);
        mockDecoder(response, RETRYABLE_EXCEPTION);

        Exception decodedException = idamErrorDecoder.decode(EMPTY_METHOD_KEY, response);

        assertThat(decodedException).isSameAs(RETRYABLE_EXCEPTION);
    }

    @Test
    void shouldReturnNewRetryableErrorWhen500ResponseStatus() {
        Response response = response(STATUS_5XX);
        mockDecoder(response, NON_RETRYABLE_EXCEPTION);

        Exception decodedException = idamErrorDecoder.decode(EMPTY_METHOD_KEY, response);
        RetryableException expectedException = new RetryableException(
            STATUS_5XX, EMPTY_REASON, GET, NON_RETRYABLE_EXCEPTION, 0L, REQUEST
        );

        assertThat(decodedException).usingRecursiveComparison().isEqualTo(expectedException);
    }

    @Test
    void shouldReturnOriginalExceptionForOtherResponseStatuses() {
        Response response = response(STATUS_NOT_5XX);
        mockDecoder(response, NON_RETRYABLE_EXCEPTION);

        Exception decodedException = idamErrorDecoder.decode(EMPTY_METHOD_KEY, response);

        assertThat(decodedException).isSameAs(NON_RETRYABLE_EXCEPTION);
    }

    private Response response(int status) {
        return Response.builder()
            .status(status)
            .reason(EMPTY_REASON)
            .request(REQUEST)
            .headers(EMPTY_HEADERS)
            .body(EMPTY_BODY)
            .build();
    }

    private void mockDecoder(Response response, Exception exception) {
        when(defaultDecoder.decode(EMPTY_METHOD_KEY, response)).thenReturn(exception);
    }
}
