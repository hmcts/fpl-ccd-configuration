package uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public final class NotifyGatekeeperTemplate extends SharedNotifyTemplate {
    public NotifyGatekeeperTemplate duplicate() {
        NotifyGatekeeperTemplate clone = NotifyGatekeeperTemplate.builder().build();

        clone.setLocalAuthority(this.getLocalAuthority());
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

