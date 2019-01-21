package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderReasonsType;

import java.util.Map;

public class EmergencyProtectionOrderReasonMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return EmergencyProtectionOrderReasonsType.valueOf((String) input).getLabel();
    }
}
