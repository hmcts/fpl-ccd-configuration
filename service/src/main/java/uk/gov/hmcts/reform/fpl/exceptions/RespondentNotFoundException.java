package uk.gov.hmcts.reform.fpl.exceptions;

import java.util.UUID;

public class RespondentNotFoundException extends RuntimeException {
    public RespondentNotFoundException(UUID respondentId) {
        super(String.format("Respondent with id %s not found", respondentId));
    }
}
