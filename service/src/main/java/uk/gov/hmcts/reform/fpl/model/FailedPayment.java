package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FailedPayment {
    private final List<ApplicationType> applicationTypes;
    private String orderApplicantType;
    private String orderApplicantName;
}
