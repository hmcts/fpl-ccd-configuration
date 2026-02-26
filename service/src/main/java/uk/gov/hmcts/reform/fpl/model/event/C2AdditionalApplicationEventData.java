package uk.gov.hmcts.reform.fpl.model.event;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

@Getter
@Jacksonized
@SuperBuilder(toBuilder = true)
public class C2AdditionalApplicationEventData extends C2DocumentBundle {
    @Temp
    private DynamicList hearingList;

    @JsonIgnore
    public C2DocumentBundle toC2DocumentBundle() {
        return super.toBuilder().build();
    }
}
