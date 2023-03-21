package uk.gov.hmcts.reform.fpl.model.event;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CustomDirection;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;

@Data
@Builder
public abstract class OrderEventData {
    @Temp
    DocumentReference urgentHearingOrderDocument;
    @Temp
    Allocation urgentHearingAllocation;
    @Temp
    YesNo showUrgentHearingAllocation;
    JudgeAndLegalAdvisor gatekeepingOrderIssuingJudge;
    GatekeepingOrderSealDecision gatekeepingOrderSealDecision;
    List<DirectionType> directionsForCafcass;
    List<DirectionType> directionsForCourt;
    List<DirectionType> directionsForCourtUpdated;

    List<Element<CustomDirection>> customDirections;
    List<Element<StandardDirection>> standardDirections;

    LanguageTranslationRequirement gatekeepingTranslationRequirements;
    LanguageTranslationRequirement urgentGatekeepingTranslationRequirements;

    DocumentReference currentSDO;
    YesNo useUploadRoute;
    YesNo useServiceRoute;

    public JudgeAndLegalAdvisor getGatekeepingOrderIssuingJudge() {
        return defaultIfNull(gatekeepingOrderIssuingJudge, JudgeAndLegalAdvisor.builder().build());
    }

    public GatekeepingOrderSealDecision getGatekeepingOrderSealDecision() {
        return defaultIfNull(gatekeepingOrderSealDecision, GatekeepingOrderSealDecision.builder().build());
    }


    public abstract List<DirectionType> getRequestedDirections();

    public static List<String> temporaryFields() {
        return getFieldsListWithAnnotation(OrderEventData.class, Temp.class).stream()
            .map(Field::getName)
            .collect(toList());
    }

    public List<Element<StandardDirection>> resetStandardDirections() {
        this.standardDirections = new ArrayList<>();
        return standardDirections;
    }
}
