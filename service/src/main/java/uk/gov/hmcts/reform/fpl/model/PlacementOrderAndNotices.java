package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlacementOrderAndNotices {
    private PlacementOrderAndNoticesType type;
    private DocumentReference document;
    private String description;

    public enum PlacementOrderAndNoticesType {
        PLACEMENT_ORDER,
        NOTICE_OF_PROCEEDINGS,
        NOTICE_OF_HEARING,
        OTHER,
        NOTICE_OF_PLACEMENT_ORDER
    }
}
