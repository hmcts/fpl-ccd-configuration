package uk.gov.hmcts.reform.fpl.exceptions.robotics;

public class RoboticsDataException extends RuntimeException {
    public RoboticsDataException(String message) {
        super(message);
    }

    public RoboticsDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
