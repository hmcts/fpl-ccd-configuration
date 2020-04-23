package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class HmctsSubmissionTemplate extends PersonalisedTemplate {
    private String court;
    private String localAuthority;
}
