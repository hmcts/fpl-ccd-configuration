package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IncorrectCourtCodeConfig {
    private String incorrectCourtCode;
    private String correctCourtCode;
    private String correctCourtName;
    private String organisationId;
}
