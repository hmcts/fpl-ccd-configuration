package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DocmosisHearing {
    private final String typeAndReason;
    private final String timeFrame;
    private final String withoutNoticeDetails;
    private final String reducedNoticeDetails;
    private final String respondentsAware;
    private final String respondentsAwareReason;
}
