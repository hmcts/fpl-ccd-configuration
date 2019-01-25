package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.YesNoDoNotKnowType;

import java.util.Map;

public class YesNoDoNotKnowMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return YesNoDoNotKnowType.valueOf((String) input).getLabel();
    }
}
