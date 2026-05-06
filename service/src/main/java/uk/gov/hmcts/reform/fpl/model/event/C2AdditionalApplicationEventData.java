package uk.gov.hmcts.reform.fpl.model.event;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.json.serializer.YesNoSerializer;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicMultiSelectList;

@Getter
@Jacksonized
@SuperBuilder(toBuilder = true)
public class C2AdditionalApplicationEventData extends C2DocumentBundle {
    @Temp
    private DynamicList hearingList;
    @Temp
    @JsonSerialize(using = YesNoSerializer.class)
    private YesNo isHearingAdjournmentRequired;
    @Temp
    DynamicMultiSelectList childSelectorForApplication;

    @JsonIgnore
    public C2DocumentBundle toC2DocumentBundle() {
        return super.toBuilder().build();
    }
}
