package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;

import java.util.Map;

public class EmergencyProtectionOrderMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return EmergencyProtectionOrdersType.valueOf((String) input).getLabel();
    }
}
