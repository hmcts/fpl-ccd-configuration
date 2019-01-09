package uk.gov.hmcts.reform.fpl.pebble;

import com.mitchellbosecke.pebble.extension.Filter;

import java.util.List;
import java.util.Map;

public abstract class ArgumentlessFilter implements Filter {

    @Override
    public List<String> getArgumentNames() {
        return null;
    }

    @Override
    public abstract Object apply(Object input, Map<String, Object> args);
}
