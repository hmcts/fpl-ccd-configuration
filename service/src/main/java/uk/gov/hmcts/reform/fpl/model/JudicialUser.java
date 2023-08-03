package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JudicialUser {

    private String idamId;
    private String personalCode;
}
