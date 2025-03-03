package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisHearing {
    private final String timeFrame;
    private final String withoutNoticeDetails;
    private final String respondentsAware;
    private final String respondentsAwareReason;
}
