package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CafcassApiApplicant {
    private String id;
    private String name;
    private String email;
    private String phone;
    private CafcassApiAddress address;
    private List<CafcassApiColleague> colleagues;
    private boolean designated;
}
