package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Data
@Builder(toBuilder = true)
public class CourtBundle {
    private String hearing;
    private List<Element<CourtBundleForHearing>> courtBundleForHearing;

    public List<Element<CourtBundleForHearing>> getCourtBundleForHearing() {
        return defaultIfNull(this.courtBundleForHearing, new ArrayList<>());
    }
}
