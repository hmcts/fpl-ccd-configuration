package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CafcassApiSolicitor {
    private String email;
    private String firstName;
    private String lastName;
    private String organisationId;
    private String organisationName;
    private CafcassApiAddress address;
}
