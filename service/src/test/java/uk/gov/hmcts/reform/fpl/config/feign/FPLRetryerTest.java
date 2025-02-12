package uk.gov.hmcts.reform.fpl.config.feign;

import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FPLRetryerTest {

    private static final byte[] EMPTY_BODY = {};
    private static final Request REQUEST = Request.create(GET, EMPTY, Map.of(), EMPTY_BODY, UTF_8, null);
    private static final RetryableException EXCEPTION = new RetryableException(
        500, "", GET, new FeignException.InternalServerError("test", REQUEST, EMPTY_BODY,
        Collections.emptyMap()), 0L, REQUEST
    );

    @Test
    void shouldPropagateExceptionWhenMaxAttemptsReached() {
        FPLRetryer retryer = new FPLRetryer(2, 50, 2);

        retryer.continueOrPropagate(EXCEPTION);

        assertThatThrownBy(() -> retryer.continueOrPropagate(EXCEPTION))
            .isEqualTo(EXCEPTION);
    }

    @Test
    void shouldAllowForMultipleContinuesWhenMaxAttemptsNotReached() {
        FPLRetryer retryer = new FPLRetryer(10, 20, 10);

        assertThat(retryer.attempt).isEqualTo(1);

        retryer.continueOrPropagate(EXCEPTION);

        assertThat(retryer.attempt).isEqualTo(2);

        retryer.continueOrPropagate(EXCEPTION);

        assertThat(retryer.attempt).isEqualTo(3);

        retryer.continueOrPropagate(EXCEPTION);

        assertThat(retryer.attempt).isEqualTo(4);
    }
}
