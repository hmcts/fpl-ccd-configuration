package uk.gov.hmcts.reform.fpl.service.robotics;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoboticsDataValidatorService {
    private final Validator validator;

    public List<String> validate(final RoboticsData roboticsData) {
        Set<ConstraintViolation<RoboticsData>> violations = validator.validate(roboticsData);

        return violations.stream().filter(Objects::nonNull)
            .map(this::formatViolationMessage)
            .collect(Collectors.toList());
    }

    private String formatViolationMessage(final ConstraintViolation<RoboticsData> violation) {
        return format("- %1$s %2$s", violation.getPropertyPath(), violation.getMessage());
    }
}
