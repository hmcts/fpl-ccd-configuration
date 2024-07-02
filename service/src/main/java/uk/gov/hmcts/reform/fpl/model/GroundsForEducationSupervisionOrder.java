package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class GroundsForEducationSupervisionOrder {
    @NotBlank(message = "Enter details for believing the ground exists")
    private final String groundDetails;
}
