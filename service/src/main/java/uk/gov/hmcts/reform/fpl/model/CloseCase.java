package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseCase {
    private LocalDate date;
    private LocalDate dateBackup;

    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String showFullReason;
    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fullReason;
    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String partialReason;
    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String details;

}
