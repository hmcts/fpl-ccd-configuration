package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason;
import uk.gov.hmcts.reform.fpl.validation.groups.CloseCaseGroup;

import java.time.LocalDate;
import javax.validation.constraints.PastOrPresent;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason.DEPRIVATION_OF_LIBERTY;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseCase {
    // This field is hidden so runs into our favourite CCD issue of not persisting, we are ignoring the
    // deserialization of the object as we can infer what it is supposed to be from which reason field is populated
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private YesNo showFullReason;
    @PastOrPresent(message = "The close case date must be in the past", groups = CloseCaseGroup.class)
    private LocalDate date;
    private String details;
    @JsonIgnore
    private CloseCaseReason reason;

    @JsonGetter("fullReason")
    public CloseCaseReason getFullReason() {
        return showFullReason == YES ? reason : null;
    }

    @JsonSetter("fullReason")
    public void setFullReason(CloseCaseReason fullReason) {
        if (reason != null) {
            this.reason = fullReason;
            this.showFullReason = YES;
        }
    }

    @JsonGetter("partialReason")
    public CloseCaseReason getPartialReason() {
        return showFullReason == NO ? reason : null;
    }

    @JsonSetter("partialReason")
    public void setPartialReason(CloseCaseReason partialReason) {
        if (partialReason != null) {
            this.reason = partialReason;
            this.showFullReason = NO;
        }
    }

    public boolean hasDeprivationOfLiberty() {
        return DEPRIVATION_OF_LIBERTY == reason;
    }
}
