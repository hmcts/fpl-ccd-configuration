package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class CafcassSubmissionTemplate extends PersonalisedTemplate {
    private String cafcass;
    private String localAuthority;
}
