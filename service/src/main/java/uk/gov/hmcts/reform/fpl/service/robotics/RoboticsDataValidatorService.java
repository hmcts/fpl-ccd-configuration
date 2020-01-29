package uk.gov.hmcts.reform.fpl.service.robotics;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.robotics.RoboticsDataException;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoboticsDataValidatorService {
    private final Validator validator;

    public List<String> validationErrors(final RoboticsData roboticsData) {
        Set<ConstraintViolation<RoboticsData>> violations = validator.validate(roboticsData);

        return violations.stream().filter(Objects::nonNull)
            .map(this::formatViolationMessage)
            .collect(Collectors.toList());
    }

    public void verifyRoboticsJsonData(final String roboticsJsonData) {
        if (isBlank(roboticsJsonData)) {
            throw new RoboticsDataException(
                "Robotics email notification failed to proceed as Json data is empty/null");
        }
    }

    private String formatViolationMessage(final ConstraintViolation<RoboticsData> violation) {
        return format("- %1$s %2$s", violation.getPropertyPath(), violation.getMessage());
    }
}
