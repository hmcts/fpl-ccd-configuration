package uk.gov.hmcts.reform.fpl.exceptions;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class CaseProgressionReportException extends RuntimeException {

    public CaseProgressionReportException(String s, Exception e) {
        super(s, e);
    }
}
