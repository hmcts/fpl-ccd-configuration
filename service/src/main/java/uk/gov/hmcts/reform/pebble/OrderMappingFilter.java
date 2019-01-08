package uk.gov.hmcts.reform.pebble;

import com.mitchellbosecke.pebble.extension.Filter;
import uk.gov.hmcts.reform.fpl.config.utils.OrderType;

import java.util.List;
import java.util.Map;


public class OrderMappingFilter implements Filter {

    @Override
    public List<String> getArgumentNames() {
        return null;
    }

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return OrderType.valueOf((String) input).getLabel();
    }
}
