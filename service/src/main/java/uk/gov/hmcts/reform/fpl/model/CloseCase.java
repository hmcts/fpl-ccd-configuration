package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason;

import java.time.LocalDate;
import javax.validation.constraints.PastOrPresent;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseCase {
    private final YesNo showFullReason;
    // TODO: 11/05/2020 error message
    @PastOrPresent
    private final LocalDate date;
    // TODO: 11/05/2020 Merge these two?
    private final CloseCaseReason fullReason;
    private final CloseCaseReason partialReason;
    private final String details;

}
