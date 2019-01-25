package uk.gov.hmcts.reform.fpl.pebble;

import uk.gov.hmcts.reform.fpl.config.utils.AllocationProposalListType;

import java.util.Map;

public class AllocationProposalMappingFilter extends ArgumentlessFilter {

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return AllocationProposalListType.valueOf((String) input).getLabel();
    }
}
