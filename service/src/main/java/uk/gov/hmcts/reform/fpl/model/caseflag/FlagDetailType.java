package uk.gov.hmcts.reform.fpl.model.caseflag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("checkstyle:MemberName") // needed for the Welsh options
public class FlagDetailType {
    private String name;
    private String name_cy;
    private String subTypeValue;
    private String subTypeValue_cy;
    private String subTypeKey;
    private String otherDescription;
    private String otherDescription_cy;
    private String flagComment;
    private String flagComment_cy;
    private String dateTimeModified;
    private String dateTimeCreated;
    private ListTypeItem<String> path;
    private String hearingRelevant;
    private String flagCode;
    private String status;
    private String requestReason;
    private String availableExternally;
}
