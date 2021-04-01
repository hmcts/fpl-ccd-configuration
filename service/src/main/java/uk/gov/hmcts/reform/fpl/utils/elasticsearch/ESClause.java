package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import java.util.Map;

public interface ESClause {
    Map<String, Object> toMap();
}
