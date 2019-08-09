package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
public class Hearing {

    @NotBlank(message = "Select an option for when you need a hearing")
    private final String type;
    private final String reason;
    private final String timeFrame;
    private final String reducedNotice;
    private final String withoutNotice;
    private final String type_GiveReason;
    private final String respondentsAware;
    private final String reducedNoticeReason;
    private final String withoutNoticeReason;
    private final String respondentsAwareReason;
}
