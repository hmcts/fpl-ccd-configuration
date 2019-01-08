package uk.gov.hmcts.reform.pebble;

import com.mitchellbosecke.pebble.extension.Filter;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionType;

import java.util.List;
import java.util.Map;


public class EmergencyProtectionOrderDirectionMappingFilter implements Filter {

    @Override
    public List<String> getArgumentNames() {
        return null;
    }

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return EmergencyProtectionOrderDirectionType.valueOf((String) input).getLabel();
    }
}
