package uk.gov.hmcts.reform.fpl.model.api.cafcass;

import lombok.Builder;
import uk.gov.hmcts.reform.fpl.model.Child;

import java.util.List;

@Builder
public class CafcassApiSearchCasesResponse {
    private int total;
    private List<CafcassApiCase> cases;
}
