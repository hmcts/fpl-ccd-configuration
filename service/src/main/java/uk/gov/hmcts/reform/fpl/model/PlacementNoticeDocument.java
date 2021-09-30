package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlacementNoticeDocument {

    private RecipientType type;
    private DocumentReference notice;
    private String noticeDescription;
    private DocumentReference response;
    private String responseDescription;
    private String recipientName;
    private UUID respondentId;

    @Getter
    @RequiredArgsConstructor
    public enum RecipientType {
        LOCAL_AUTHORITY("Local authority"),
        CAFCASS("Cafcass"),
        PARENT_FIRST("First parent"),
        PARENT_SECOND("Second parent");

        private final String name;
    }

}
