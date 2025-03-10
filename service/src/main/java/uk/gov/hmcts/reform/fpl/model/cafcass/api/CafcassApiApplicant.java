package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CafcassApiApplicant {
    private String id;
    private String name;
    private String email;
    private String phone;
    private CafcassApiAddress address;
    private List<CafcassApiColleague> colleagues;
    private Boolean designated;
}
