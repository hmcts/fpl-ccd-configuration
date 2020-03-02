package uk.gov.hmcts.reform.fnp.exception;

import lombok.Getter;

@Getter
public class FeeRegisterException extends RuntimeException {

    private final int status;

    public FeeRegisterException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
