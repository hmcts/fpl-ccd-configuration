package uk.gov.hmcts.reform.fpl.exceptions;

public class CaseNotOutsourcedException extends RuntimeException {

    public CaseNotOutsourcedException(Long caseId) {
        super(String.format("Case %d is not outsourced", caseId));
    }
}
