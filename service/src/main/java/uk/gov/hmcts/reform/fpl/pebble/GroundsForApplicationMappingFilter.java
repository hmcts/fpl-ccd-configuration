package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.GroundsForApplicationType;

import java.util.Map;

public class GroundsForApplicationMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return GroundsForApplicationType.valueOf((String) input).getLabel();
    }
}
