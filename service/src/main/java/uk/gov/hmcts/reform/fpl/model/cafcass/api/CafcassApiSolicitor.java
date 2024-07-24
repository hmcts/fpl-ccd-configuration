package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class CafcassApiSolicitor {
    private String email;
    private String firstName;
    private String lastName;
    private String organisationId;
    private String organisationName;
}
