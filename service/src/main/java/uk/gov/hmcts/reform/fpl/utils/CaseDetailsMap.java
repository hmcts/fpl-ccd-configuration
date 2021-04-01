package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CaseDetailsMap extends HashMap<String, Object> {

    private CaseDetailsMap(Map<? extends String, ?> map) {
        super(map);
    }

    public CaseDetailsMap putIfNotEmpty(String key, Object value) {
        if (ObjectUtils.isEmpty(value)) {
            this.remove(key);
        } else {
            this.put(key, value);
        }
        return this;
    }

    public CaseDetailsMap putIfNotEmpty(Map<String, Object> map) {
        map.forEach(this::putIfNotEmpty);
        return this;
    }

    public static CaseDetailsMap caseDetailsMap(CaseDetails caseDetail) {
        return new CaseDetailsMap(caseDetail.getData());
    }

    public void removeAll(String... keys) {
        Stream.of(keys).forEach(this::remove);
    }
}
