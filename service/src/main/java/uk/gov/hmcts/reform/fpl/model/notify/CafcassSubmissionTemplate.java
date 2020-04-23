package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class CafcassSubmissionTemplate extends PersonalisedTemplate {
    private String cafcass;
    private String localAuthority;
}
