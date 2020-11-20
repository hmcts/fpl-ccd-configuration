package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

public class CaseDetailsMap extends HashMap<String, Object> {

    private CaseDetailsMap(Map<? extends String, ?> map) {
        super(map);
    }

    public CaseDetailsMap putIfNotEmpty(String key, Object value) {
        updateOrRemoveIfEmpty(this, key, value);
        return this;
    }

    public static void updateOrRemoveIfEmpty(Map<String, Object> map, String key, Object value) {
        if (ObjectUtils.isEmpty(value)) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
    }

    public static CaseDetailsMap caseDetailsMap(CaseDetails caseDetail) {
        return new CaseDetailsMap(caseDetail.getData());
    }
}
