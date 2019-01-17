package uk.gov.hmcts.reform.fpl.pebble;

import com.google.common.collect.ImmutableList;
import com.mitchellbosecke.pebble.extension.Filter;

import java.util.List;

public abstract class ArgumentlessFilter implements Filter {

    @Override
    public List<String> getArgumentNames() {
        return ImmutableList.<String>builder().build();
    }

}
