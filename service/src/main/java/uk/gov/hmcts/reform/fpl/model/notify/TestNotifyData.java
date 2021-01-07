package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TestNotifyData implements NotifyData {

    String fieldA;
    String fieldB;

}
