package uk.gov.hmcts.reform.fpl.model.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class GatekeeperNotificationTemplate extends PersonalisedTemplate {
    private String localAuthority;
    @JsonProperty("gatekeeper_recipients")
    private String gatekeeperRecipients;

    public GatekeeperNotificationTemplate duplicate() {
        GatekeeperNotificationTemplate clone = new GatekeeperNotificationTemplate();

        clone.setLocalAuthority(this.getLocalAuthority());
        clone.setGatekeeperRecipients(this.getGatekeeperRecipients());
        clone.setOrdersAndDirections(this.getOrdersAndDirections());
        clone.setDataPresent(this.getDataPresent());
        clone.setFullStop(this.getFullStop());
        clone.setTimeFramePresent(this.getTimeFramePresent());
        clone.setUrgentHearing(this.getUrgentHearing());
        clone.setNonUrgentHearing(this.getNonUrgentHearing());
        clone.setFirstRespondentName(this.getFirstRespondentName());
        clone.setReference(this.getReference());
        clone.setCaseUrl(this.getCaseUrl());

        return clone;
    }
}

