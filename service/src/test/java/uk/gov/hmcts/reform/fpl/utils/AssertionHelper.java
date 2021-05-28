package uk.gov.hmcts.reform.fpl.utils;

import org.awaitility.core.ConditionTimeoutException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

import static org.awaitility.Awaitility.await;

public class AssertionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssertionHelper.class);

    private static final Duration POLL_INTERVAL = Duration.ofMillis(100);

    private static final Duration ASYNC_MAX_DELAY = Duration.ofSeconds(1);
    public static final long ASYNC_MAX_TIMEOUT = ASYNC_MAX_DELAY.toMillis();

    private AssertionHelper() {
    }

    //Used for asserting maps that contain JSONObjects (i.e document attachments for GOV.Notify notifications)
    public static void assertEquals(Map<String, Object> actual, Map<String, Object> expected) {
        JSONAssert.assertEquals(new JSONObject(actual), new JSONObject(expected), true);
    }

    /**
     * Supports verification of asynchronous execution
     * Polls until verification function runs without exceptions for max 1[s].
     * example usage: checkUntil(() -> verify(mock).method())
     */
    public static void checkUntil(ThrowableRunnable verification) {
        await().pollInterval(POLL_INTERVAL).atMost(ASYNC_MAX_DELAY)
            .ignoreExceptions()
            .until(() -> {
                verification.run();
                return true;
            });
    }

    /**
     * Supports verification of asynchronous execution
     * Polls until verification function throws exceptions or for 1[s].
     * Checks that verification is met all the time within 1[s] period and fails fast as soon as condition is not met
     * Useful for negative scenarios like:
     * example usage: checkThat(() -> {verify(mock1, never()).method(); verify(mock2, never()).method())
     */
    public static void checkThat(ThrowableRunnable verification) {
        try {
            await().pollInterval(POLL_INTERVAL).atMost(ASYNC_MAX_DELAY)
                .until(() -> {
                    verification.run();
                    return false;
                });
        } catch (ConditionTimeoutException e) {
            LOGGER.debug("Verification holds for {}", ASYNC_MAX_DELAY);
        }
    }

    @FunctionalInterface
    public interface ThrowableRunnable {
        void run() throws Exception;
    }
}
