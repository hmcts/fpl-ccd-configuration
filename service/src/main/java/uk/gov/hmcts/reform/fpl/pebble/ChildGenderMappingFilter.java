package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.ChildGenderListType;

import java.util.Map;

public class ChildGenderMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return ChildGenderListType.valueOf((String) input).getLabel();
    }
}
