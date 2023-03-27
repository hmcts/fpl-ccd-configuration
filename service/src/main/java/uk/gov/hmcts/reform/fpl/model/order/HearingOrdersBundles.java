package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class HearingOrdersBundles {
    private List<Element<HearingOrdersBundle>> agreedCmos;
    private List<Element<HearingOrdersBundle>> draftCmos;
}
