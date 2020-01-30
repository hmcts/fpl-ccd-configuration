package uk.gov.hmcts.reform.fpl.service.robotics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.service.robotics.SampleRoboticsTestDataHelper.expectedRoboticsData;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RoboticsDataValidatorService.class, ValidationAutoConfiguration.class})
public class RoboticsDataValidatorServiceTest {

    @Autowired
    private RoboticsDataValidatorService roboticsDataValidatorService;

    @Test
    void shouldNotReturnValidationViolationsWhenRoboticDataIsValid() {
        RoboticsData roboticsData = expectedRoboticsData(CARE_ORDER.getLabel());

        List<String> returnedViolations = roboticsDataValidatorService.validate(roboticsData);

        assertThat(returnedViolations).isEmpty();
    }

    @Test
    void shouldReturnValidationErrorWhenRoboticsDataApplicationTypeIsNull() {
        RoboticsData roboticsData = expectedRoboticsData(null);

        List<String> returnedViolations = roboticsDataValidatorService.validate(roboticsData);

        assertThat(returnedViolations).containsExactly("- applicationType value should not be null/empty");
    }

    @Test
    void shouldReturnValidationErrorWhenRoboticsDataAllocationIsNull() {
        RoboticsData roboticsData = expectedRoboticsData(SUPERVISION_ORDER.getLabel());
        RoboticsData updatedRoboticsData = roboticsData.toBuilder().allocation(null).build();

        List<String> returnedViolations = roboticsDataValidatorService.validate(updatedRoboticsData);

        assertThat(returnedViolations).containsExactly("- allocation value should not be null/empty");
    }

    @Test
    void shouldReturnValidationErrorsWhenRoboticsDataOwningCourtIsZeroOrIssueDateIsEmpty() {
        RoboticsData roboticsData = expectedRoboticsData(SUPERVISION_ORDER.getLabel());
        RoboticsData updatedRoboticsData = roboticsData.toBuilder()
            .issueDate("")
            .owningCourt(0)
            .build();

        List<String> returnedViolations = roboticsDataValidatorService.validate(updatedRoboticsData);

        assertThat(returnedViolations).contains("- issueDate value should not be null/empty",
            "- owningCourt value should be greater than 0");
    }
}
