package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CafcassSubmissionTemplate extends NotifyCaseContent {
    private String cafcass;
    private String localAuthority;

//    public CafcassSubmissionTemplate(List<String> ordersAndDirections,
//                                     YesNo dataPresent,
//                                     YesNo fullStop,
//                                     YesNo timeFramePresent,
//                                     YesNo urgentHearing,
//                                     YesNo nonUrgentHearing,
//                                     String firstRespondentName,
//                                     String reference,
//                                     String caseUrl,
//                                     String cafcass,
//                                     String localAuthority) {
//        super(ordersAndDirections, dataPresent, fullStop, timeFramePresent, urgentHearing, nonUrgentHearing,
//            firstRespondentName, reference, caseUrl);
//
//        this.cafcass = cafcass;
//        this.localAuthority = localAuthority;
//    }
}
