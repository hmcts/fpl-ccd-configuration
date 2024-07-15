package uk.gov.hmcts.reform.fpl.model.api.cafcass;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CafcassApiSolicitor {
    private String email;
    private String firstName;
    private String lastName;
    private String organisationId;
    private String organisationName;
}
