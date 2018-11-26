package uk.gov.hmcts.reform.pebble;

import com.google.common.collect.ImmutableList;
import com.mitchellbosecke.pebble.extension.Filter;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Filter returns today's date as an instance of {@link java.util.Date} class.
 */
public class TodayFilter implements Filter {

    @Override
    public List<String> getArgumentNames() {
        return ImmutableList.<String>builder().build();
    }

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return new Date();
    }
}
