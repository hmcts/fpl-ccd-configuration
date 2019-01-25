package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.HearingTimeFrameType;

import java.util.Map;

public class HearingTimeFrameMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return HearingTimeFrameType.valueOf((String) input).getLabel();
    }
}
