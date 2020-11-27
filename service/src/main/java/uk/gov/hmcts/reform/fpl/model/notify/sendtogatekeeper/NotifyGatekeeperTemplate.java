package uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public final class NotifyGatekeeperTemplate extends SharedNotifyTemplate {
    public SharedNotifyTemplate duplicate() {
        return SharedNotifyTemplate.builder()
            .localAuthority(this.getLocalAuthority())
            .ordersAndDirections(this.getOrdersAndDirections())
            .dataPresent(this.getDataPresent())
            .fullStop(this.getFullStop())
            .timeFramePresent(this.getTimeFramePresent())
            .timeFrameValue(this.getTimeFrameValue())
            .urgentHearing(this.getUrgentHearing())
            .nonUrgentHearing(this.getNonUrgentHearing())
            .firstRespondentName(this.getFirstRespondentName())
            .reference(this.getReference())
            .caseUrl(this.getCaseUrl())
            .build();
    }
}

