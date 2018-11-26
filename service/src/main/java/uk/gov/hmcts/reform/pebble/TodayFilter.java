package uk.gov.hmcts.reform.pebble;

import com.mitchellbosecke.pebble.extension.Filter;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TodayFilter implements Filter {
    @Override
    public List<String> getArgumentNames() {
        return Collections.emptyList();
    }

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return new Date();
    }
}
