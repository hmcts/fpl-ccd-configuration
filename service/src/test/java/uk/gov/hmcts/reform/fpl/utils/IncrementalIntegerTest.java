package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IncrementalIntegerTest {
    @Test
    void shouldGetAndIncrementCurrentIntegerValue() {
        IncrementalInteger i = new IncrementalInteger();
        assertThat(i.getAndIncrement()).isZero();
    }

    @Test
    void shouldIncrementAndGetCurrentIntegerValue() {
        IncrementalInteger i = new IncrementalInteger();
        assertThat(i.incrementAndGet()).isEqualTo(1);
    }
}
