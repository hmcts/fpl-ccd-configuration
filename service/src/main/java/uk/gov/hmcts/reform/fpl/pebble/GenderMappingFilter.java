package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.GenderListType;

import java.util.Map;

public class GenderMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return GenderListType.valueOf((String) input).getLabel();
    }
}
