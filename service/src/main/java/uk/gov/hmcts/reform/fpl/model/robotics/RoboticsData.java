package uk.gov.hmcts.reform.fpl.model.robotics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RoboticsData {
    @NotEmpty(message = "value should not be null/empty")
    private final String caseNumber;

    @NotEmpty(message = "value should not be null/empty")
    private final String applicationType;

    @Positive(message = "value should be greater than 0")
    private final double feePaid;

    private final Set<Child> children;
    private final Set<Respondent> respondents;
    private final Solicitor solicitor;

    private final boolean harmAlleged;
    private final boolean internationalElement;

    @NotEmpty(message = "value should not be null/empty")
    private final String allocation;

    @NotEmpty(message = "value should not be null/empty")
    private final String issueDate;

    @Valid
    private final Applicant applicant;

    @Positive(message = "value should be greater than 0")
    private final int owningCourt;

    @JsonIgnore
    private final Long caseId;
}
