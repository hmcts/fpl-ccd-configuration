package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.AttachedToFollowOptionType;

import java.util.Map;

public class AttachedToFollowOptionMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return AttachedToFollowOptionType.valueOf((String) input).getLabel();
    }
}
