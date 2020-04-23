package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class HmctsSubmissionTemplate extends PersonalisedTemplate {
    private String court;
    private String localAuthority;
}
