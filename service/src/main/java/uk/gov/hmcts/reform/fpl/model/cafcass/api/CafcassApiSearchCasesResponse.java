package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CafcassApiSearchCasesResponse {
    private int total;
    private List<CafcassApiCase> cases;
}
