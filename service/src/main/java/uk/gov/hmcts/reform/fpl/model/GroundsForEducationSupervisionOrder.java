package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class GroundsForEducationSupervisionOrder {
    @NotBlank(message = "Enter details for believing the ground exists")
    private final String groundDetails;
}
