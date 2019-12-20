package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class Others {
    //firstOther is redundant but must be maintained for past cases. If exists, add to additionalOthers as first element
    private final Other firstOther;
    private final List<Element<Other>> additionalOthers;
}
