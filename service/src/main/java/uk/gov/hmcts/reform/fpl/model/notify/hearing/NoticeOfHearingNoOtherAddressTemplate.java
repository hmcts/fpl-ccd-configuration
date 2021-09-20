package uk.gov.hmcts.reform.fpl.model.notify.hearing;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
public class NoticeOfHearingNoOtherAddressTemplate extends BaseCaseNotifyData {
    private String familyManCaseNumber;
    private String ccdNumber;
    private String hearingType;
    private String hearingDate;
    private String partyName;
}
