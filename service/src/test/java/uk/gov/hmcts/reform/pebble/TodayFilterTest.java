package uk.gov.hmcts.reform.pebble;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class TodayFilterTest {

    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private TodayFilter filter = new TodayFilter();

    @Test
    void shouldReturnTodaysDate() {
        Object date = filter.apply("", NO_ARGS);
        assertThat((Date) date).isToday();
    }
}
