package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.LivingSituationListType;

import java.util.Map;

public class LivingSituationMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return LivingSituationListType.valueOf((String) input).getLabel();
    }
}
