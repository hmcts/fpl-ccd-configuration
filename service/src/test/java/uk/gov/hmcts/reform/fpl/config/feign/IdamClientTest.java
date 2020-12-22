package uk.gov.hmcts.reform.fpl.config.feign;

import feign.Client;
import feign.ExceptionPropagationPolicy;
import feign.Feign;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import uk.gov.hmcts.reform.fpl.config.feign.codec.IdamErrorDecoder;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdamClientTest {
    private static final Map<String, Collection<String>> EMPTY_HEADERS = Map.of();
    private static final String EMPTY_REASON = "";
    private static final byte[] EMPTY_BODY = new byte[0];
    private static final Request REQUEST = Request.create(GET, EMPTY, Map.of(), new byte[] {}, UTF_8, null);

    private final Client httpClient = mock(Client.Default.class);
    private final IdamApi idamApi = Feign.builder()
        .errorDecoder(new IdamErrorDecoder())
        .retryer(new FPLRetryer(2, 20, 3))
        .contract(new SpringMvcContract())
        .exceptionPropagationPolicy(ExceptionPropagationPolicy.UNWRAP)
        .client(httpClient)
        .target(IdamApi.class, "http://localhost:1234");

    private final IdamClient idamClient = new IdamClient(idamApi, mock(OAuth2Configuration.class));

    @Test
    void shouldRetryWhen5XXResponseReturned() throws IOException {
        when(httpClient.execute(any(), any()))
            .thenReturn(response(500), response(504), response(204));

        idamClient.getUserDetails("details");

        verify(httpClient, times(3)).execute(any(), any());
    }

    @Test
    void shouldReturnFeignExceptionWhenMaxRetriesReached() throws IOException {
        when(httpClient.execute(any(), any()))
            .thenReturn(response(500), response(500), response(500));

        assertThatThrownBy(() -> idamClient.getUserDetails("details"))
            .isInstanceOf(FeignException.InternalServerError.class);

        verify(httpClient, times(3)).execute(any(), any());
    }

    @Test
    void shouldNotRetryGivenSuccessfulResponse() throws IOException {
        when(httpClient.execute(any(), any()))
            .thenReturn(response(204));

        idamClient.getUserDetails("details");

        verify(httpClient).execute(any(), any());
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
}
