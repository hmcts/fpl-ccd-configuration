package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicElementIndicator;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingDateDynamicElement implements DynamicElementIndicator {
    private final String date;
    private final UUID id;

    @Override
    public DynamicListElement toDynamicElement() {
        return DynamicListElement.builder().code(id).label(date).build();
    }
}
