package uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@Getter
@Setter
public final class NotifyGatekeeperTemplate extends SharedNotifyTemplate {
    @JsonProperty("gatekeeper_recipients")
    private String gatekeeperRecipients;

    public NotifyGatekeeperTemplate duplicate() {
        NotifyGatekeeperTemplate clone = new NotifyGatekeeperTemplate();

        clone.setLocalAuthority(this.getLocalAuthority());
        clone.setGatekeeperRecipients(this.getGatekeeperRecipients());
        clone.setOrdersAndDirections(this.getOrdersAndDirections());
        clone.setDataPresent(this.getDataPresent());
        clone.setFullStop(this.getFullStop());
        clone.setTimeFramePresent(this.getTimeFramePresent());
        clone.setTimeFrameValue(this.getTimeFrameValue());
        clone.setUrgentHearing(this.getUrgentHearing());
        clone.setNonUrgentHearing(this.getNonUrgentHearing());
        clone.setFirstRespondentName(this.getFirstRespondentName());
        clone.setReference(this.getReference());
        clone.setCaseUrl(this.getCaseUrl());

        return clone;
    }
}

