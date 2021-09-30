package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
@Data
public class PlacementOrderIssuedNotifyData extends OrderIssuedNotifyData {

    private String childLastName;

}
