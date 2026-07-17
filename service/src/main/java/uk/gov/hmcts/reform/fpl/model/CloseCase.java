package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseCase {
    @CCD(label = "Date", hint = "For example, 31 3 1980")
    private LocalDate date;
    @CCD(label = "Date", showCondition = "date=\"DO_NOT_SHOW\"")
    private LocalDate dateBackup;

    @CCD(label = " ", showCondition = "date=\"DO_NOT_SHOW\"")
    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String showFullReason;
    @CCD(label = "Reason", showCondition = "showFullReason!=\"NO\"")
    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fullReason;
    @CCD(label = "Reason", showCondition = "showFullReason!=\"YES\"")
    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String partialReason;
    @CCD(
            label = "Closure details",
            showCondition = "fullReason=\"OTHER\" OR partialReason=\"OTHER\"",
            typeOverride = FieldType.TextArea
    )
    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String details;

}
