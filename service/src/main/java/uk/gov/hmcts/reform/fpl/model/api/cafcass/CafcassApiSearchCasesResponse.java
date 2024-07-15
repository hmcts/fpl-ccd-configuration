package uk.gov.hmcts.reform.fpl.model.api.cafcass;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CafcassApiSearchCasesResponse {
    private int total;
    private List<CafcassApiCase> cases;
}
