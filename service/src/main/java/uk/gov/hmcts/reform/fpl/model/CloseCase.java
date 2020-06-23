package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.types.CCD;
import uk.gov.hmcts.ccd.sdk.types.FieldType;
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
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseCase {
    // This field is hidden so runs into our favourite CCD issue of not persisting, we are ignoring the
    // deserialization of the object as we can infer what it is supposed to be from which reason field is populated
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @CCD(type = FieldType.YesOrNo)
    private YesNo showFullReason;
    @PastOrPresent(message = "The close case date must be in the past", groups = CloseCaseGroup.class)
    private LocalDate date;
    private String details;
    @JsonIgnore
    private CloseCaseReason reason;

    @JsonProperty("fullReason")
    public CloseCaseReason getFullReason() {
        return showFullReason == YES ? reason : null;
    }

    @JsonProperty("fullReason")
    public void setFullReason(CloseCaseReason fullReason) {
        if (fullReason != null) {
            this.reason = fullReason;
            this.showFullReason = YES;
        }
    }

    @JsonProperty("partialReason")
    public CloseCaseReason getPartialReason() {
        return showFullReason == NO ? reason : null;
    }

    @JsonProperty("partialReason")
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
