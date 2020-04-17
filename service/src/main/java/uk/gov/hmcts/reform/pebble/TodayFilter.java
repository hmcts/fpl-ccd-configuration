package uk.gov.hmcts.reform.pebble;

import com.google.common.collect.ImmutableList;
import com.mitchellbosecke.pebble.extension.Filter;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Filter returns today's date as an instance of {@link java.util.Date} class.
 */
public class TodayFilter implements Filter {

    private final Clock clock;

    public TodayFilter() {
        this(Clock.systemDefaultZone());
    }

    public TodayFilter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public List<String> getArgumentNames() {
        return ImmutableList.<String>builder().build();
    }

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return Date.from(ZonedDateTime.now(this.clock).toInstant());
    }
}
