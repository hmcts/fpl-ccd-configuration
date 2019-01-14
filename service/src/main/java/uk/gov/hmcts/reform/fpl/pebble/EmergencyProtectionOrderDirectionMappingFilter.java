package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;

import java.util.Map;

public class EmergencyProtectionOrderDirectionMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return EmergencyProtectionOrderDirectionsType.valueOf((String) input).getLabel();
    }
}
