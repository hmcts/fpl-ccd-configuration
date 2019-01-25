package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.FutureOrPastHarmSelectType;

import java.util.Map;

public class FutureOrPastHarmSelectMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return FutureOrPastHarmSelectType.valueOf((String) input).getLabel();
    }
}
