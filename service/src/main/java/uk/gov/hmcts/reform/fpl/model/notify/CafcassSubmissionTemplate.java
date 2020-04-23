package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.YesNo;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class CafcassSubmissionTemplate extends NotifyCaseContent {
    private final String cafcass;
    private final String localAuthority;

    @Builder
    private CafcassSubmissionTemplate(List<String> ordersAndDirections,
                                      YesNo dataPresent,
                                      YesNo fullStop,
                                      YesNo timeFramePresent,
                                      YesNo urgentHearing,
                                      YesNo nonUrgentHearing,
                                      String firstRespondentName,
                                      String reference,
                                      String caseUrl,
                                      String cafcass,
                                      String localAuthority) {
        super(ordersAndDirections, dataPresent, fullStop, timeFramePresent, urgentHearing, nonUrgentHearing,
            firstRespondentName, reference, caseUrl);

        this.cafcass = cafcass;
        this.localAuthority = localAuthority;
    }
}
