package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;

@Data
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

    JudgeAndLegalAdvisor gatekeepingOrderIssuingJudge;
    GatekeepingOrderSealDecision gatekeepingOrderSealDecision;

    List<DirectionType> directionsForAllParties;
    List<DirectionType> directionsForCourt;
    List<DirectionType> directionsForCourtUpdated;
    List<DirectionType> directionsForLocalAuthority;
    List<DirectionType> directionsForCafcass;
    List<DirectionType> directionsForCafcassUpdated;
    List<DirectionType> directionsForRespondents;
    List<DirectionType> directionsForOthers;

    List<DirectionType> urgentDirectionsForAllParties;
    List<DirectionType> urgentDirectionsForLocalAuthority;
    List<DirectionType> urgentDirectionsForCafcass;


    List<Element<CustomDirection>> customDirections;
    List<Element<StandardDirection>> standardDirections;

    String gatekeepingOrderListOrSendToAdminReason;
    String gatekeepingOrderListOrSendToAdmin;

    LanguageTranslationRequirement gatekeepingTranslationRequirements;
    LanguageTranslationRequirement urgentGatekeepingTranslationRequirements;

    DocumentReference currentSDO;
    YesNo useUploadRoute;
    YesNo useServiceRoute;

    @JsonIgnore
    public List<DirectionType> getRequestedDirections() {
        return Stream.of(urgentDirectionsForAllParties, urgentDirectionsForLocalAuthority, urgentDirectionsForCafcass,
                directionsForAllParties, directionsForLocalAuthority, directionsForCafcass, directionsForCourt,
                directionsForCafcassUpdated, directionsForCourtUpdated, directionsForRespondents, directionsForOthers)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(toList());
    }

    public JudgeAndLegalAdvisor getGatekeepingOrderIssuingJudge() {
        return defaultIfNull(gatekeepingOrderIssuingJudge, JudgeAndLegalAdvisor.builder().build());
    }

    public GatekeepingOrderSealDecision getGatekeepingOrderSealDecision() {
        return defaultIfNull(gatekeepingOrderSealDecision, GatekeepingOrderSealDecision.builder().build());
    }

    public static List<String> temporaryFields() {
        return getFieldsListWithAnnotation(GatekeepingOrderEventData.class, Temp.class).stream()
            .map(Field::getName)
            .collect(toList());
    }

    public List<Element<StandardDirection>> resetStandardDirections() {
        this.standardDirections = new ArrayList<>();
        return standardDirections;
    }

    @JsonIgnore
    public boolean isSentToAdmin() {
        return Optional.ofNullable(gatekeepingOrderListOrSendToAdmin)
            .map(value -> value.equals("NO"))
            .orElse(false);
    }

    @JsonIgnore
    public String getSendToAdminReason() {
        return gatekeepingOrderListOrSendToAdminReason;
    }
}
