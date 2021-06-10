package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CustomDirection;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.lang.reflect.Field;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatekeepingOrderEventData {
    @Temp
    DocumentReference urgentHearingOrderDocument;
    @Temp
    Allocation urgentHearingAllocation;
    @Temp
    YesNo showUrgentHearingAllocation;

    List<Element<CustomDirection>> sdoDirectionCustom;
    JudgeAndLegalAdvisor gatekeepingOrderIssuingJudge;
    GatekeepingOrderSealDecision gatekeepingOrderSealDecision;

    public JudgeAndLegalAdvisor getGatekeepingOrderIssuingJudge() {
        return defaultIfNull(gatekeepingOrderIssuingJudge, JudgeAndLegalAdvisor.builder().build());
    }

    public GatekeepingOrderSealDecision getGatekeepingOrderSealDecision() {
        return defaultIfNull(gatekeepingOrderSealDecision, GatekeepingOrderSealDecision.builder().build());
    }

    public static String[] temporaryFields() {
        return getFieldsListWithAnnotation(GatekeepingOrderEventData.class, Temp.class).stream()
            .map(Field::getName)
            .toArray(String[]::new);
    }
}
