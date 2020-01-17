package uk.gov.hmcts.reform.fpl.model.robotics;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RoboticsData {
    private final String caseNumber;
    private final String applicationType;
    private final double feePaid;
    private final Set<Child> children;
    private final Set<Respondent> respondents;
    private final Solicitor solicitor;
    private final boolean harmAlleged;
    private final boolean internationalElement;
    private final String allocation;
    private final String issueDate;
    private final Applicant applicant;
    private final int owningCourt;
}
