package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.HearingType;

import java.util.Map;

public class HearingTypeMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return HearingType.valueOf((String) input).getLabel();
    }
}
