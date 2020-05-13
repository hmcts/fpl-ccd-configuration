package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason;
import uk.gov.hmcts.reform.fpl.validation.groups.CloseCaseGroup;

import java.time.LocalDate;
import javax.validation.constraints.PastOrPresent;

import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason.DEPRIVATION_OF_LIBERTY;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason.FINAL_ORDER;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseCase {
    private final YesNo showFullReason;
    @PastOrPresent(message = "The close case date must be in the past", groups = CloseCaseGroup.class)
    private final LocalDate date;
    @JsonIgnore
    private CloseCaseReason reason;
    private final String details;

    @JsonSetter("fullReason")
    public void setFullReason(CloseCaseReason fullReason) {
        if (reason != null) {
            this.reason = fullReason;
        }

    }

    @JsonSetter("partialReason")
    public void setPartialReason(CloseCaseReason partialReason) {
        if (partialReason != null) {
            this.reason = partialReason;
        }
    }

    @JsonGetter("fullReason")
    public CloseCaseReason getFullReason() {
        return reason;
    }

    @JsonGetter("partialReason")
    public CloseCaseReason getPartialReason() {
        return reason == FINAL_ORDER ? null : reason;
    }

    public boolean hasDeprivationOfLiberty() {
        return DEPRIVATION_OF_LIBERTY == reason;
    }
}
