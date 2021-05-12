package uk.gov.hmcts.reform.fpl.util;

import java.util.concurrent.Callable;
import java.util.function.Predicate;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class Poller {

    private Poller() {
    }

    public static <T> T poll(Callable<T> producer, Predicate<T> resultPredicate) {
        return await()
            .pollDelay(0, SECONDS)
            .pollInterval(1, SECONDS)
            .atMost(10, SECONDS)
            .until(producer, resultPredicate);
    }

}
