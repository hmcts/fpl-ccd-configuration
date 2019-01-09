package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.OrderType;

import java.util.Map;

public class OrderMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return OrderType.valueOf((String) input).getLabel();
    }
}
