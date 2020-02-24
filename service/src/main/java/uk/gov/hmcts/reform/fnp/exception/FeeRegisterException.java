package uk.gov.hmcts.reform.fnp.exception;

public class FeeRegisterException extends RuntimeException {

    private final int status;

    public FeeRegisterException() {
        super();
        this.status =  -1;
    }

    public FeeRegisterException(int status) {
        super();
        this.status = status;
    }

    public FeeRegisterException(int status, String message) {
        super(message);
        this.status = status;
    }

    public FeeRegisterException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public FeeRegisterException(int status, Throwable cause) {
        super(cause);
        this.status = status;
    }
}
