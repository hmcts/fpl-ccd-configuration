package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.lang.reflect.Field;

import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatekeepingOrderEventData {
    @Temp
    DocumentReference urgentHearingOrderDocument;
    @Temp
    Allocation urgentHearingAllocation;
    @Temp
    YesNo showUrgentHearingAllocation;

    public static String[] temporaryFields() {
        return getFieldsListWithAnnotation(GatekeepingOrderEventData.class, Temp.class).stream()
            .map(Field::getName)
            .toArray(String[]::new);
    }
}
