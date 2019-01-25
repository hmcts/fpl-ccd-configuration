package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.ProceedingStatusType;

import java.util.Map;

public class ProceedingStatusMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return ProceedingStatusType.valueOf((String) input).getLabel();
    }
}
