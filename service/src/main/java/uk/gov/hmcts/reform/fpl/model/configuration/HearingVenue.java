package uk.gov.hmcts.reform.fpl.model.configuration;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicElementParser;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

@Data
@Builder
public class HearingVenue implements DynamicElementParser {

    private final int hearingVenueId;
    private final String title;

    @Override
    public DynamicListElement toDynamicElement() {
        return DynamicListElement.builder().code(String.valueOf(hearingVenueId)).label(title).build();
    }
}
