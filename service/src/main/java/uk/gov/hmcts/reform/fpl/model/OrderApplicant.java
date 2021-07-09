package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ApplicantType;

@Data
@Builder(toBuilder = true)
public class OrderApplicant {
    private ApplicantType type;
    private String name;
}
