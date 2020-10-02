package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IncrementalIntegerTest {
    @Test
    void shouldGetAndIncrementCurrentIntegerValue() {
        IncrementalInteger i = new IncrementalInteger();
        assertThat(i.getAndIncrement()).isZero();
        assertThat(i.getValue()).isEqualTo(1);
    }

    @Test
    void shouldIncrementAndGetCurrentIntegerValue() {
        IncrementalInteger i = new IncrementalInteger();
        assertThat(i.incrementAndGet()).isEqualTo(1);
        assertThat(i.getValue()).isEqualTo(1);
    }
}
