package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.json.deserializer.DataDeserializer;
import uk.gov.hmcts.reform.fpl.json.deserializer.StandardDirectionDeserializer;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CustomDirection;
import uk.gov.hmcts.reform.fpl.model.SaveOrSendGatekeepingOrder;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    SaveOrSendGatekeepingOrder saveOrSendGatekeepingOrder;

    List<DirectionType> sdoDirectionsForAll;
    List<DirectionType> sdoDirectionsForLocalAuthority;
    List<DirectionType> sdoDirectionsForRespondents;
    List<DirectionType> sdoDirectionsForCafcass;
    List<DirectionType> sdoDirectionsForOthers;
    List<DirectionType> sdoDirectionsForCourt;
    @JsonProperty
    List<Element<CustomDirection>> sdoDirectionCustom;
    @JsonProperty
    List<Element<StandardDirection>> standardDirections;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE")
    StandardDirection sdoDirectionRequestPermissionForExpertEvidence;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-REQUEST_HELP_TO_TAKE_PART_IN_PROCEEDINGS")
    StandardDirection sdoDirectionRequestHelpToTakePartInProceedeings;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-ASK_FOR_DISCLOSURE")
    StandardDirection sdoDirectionAskForDisclousure;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-ATTEND_HEARING")
    StandardDirection sdoDirectionAttendHearing;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-CONTACT_ALTERNATIVE_CARERS")
    StandardDirection sdoDirectionContactAlternativeCarers;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-SEND_DOCUMENTS_TO_ALL_PARTIES")
    StandardDirection sdoDirectionSendDocumentsToAllParties;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-SEND_MISSING_ANNEX")
    StandardDirection sdoDirectionSendMissingAnnex;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-IDENTIFY_ALTERNATIVE_CARERS")
    StandardDirection sdoDirectionIdentifyAlternativeCarers;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-SEND_TRANSLATED_DOCUMENTS")
    StandardDirection sdoDirectionSendTransaletedDocuments;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-LODGE_BUNDLE")
    StandardDirection sdoDirectionLodgeBundle;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-SEND_CASE_SUMMARY")
    StandardDirection sdoDirectionSendCaseSummary;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-CONSIDER_JURISDICTION")
    StandardDirection sdoDirectionConsiderJurisdiction;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-SEND_RESPONSE_TO_THRESHOLD_STATEMENT")
    StandardDirection sdoDirectionSendResponseToThresholdStatement;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-ARRANGE_ADVOCATES_MEETING")
    StandardDirection sdoDirectionArrangeAdvocatesMeeting;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-SEND_GUARDIANS_ANALYSIS")
    StandardDirection sdoDirectionSendGuardianAnalysis;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-APPOINT_CHILDREN_GUARDIAN")
    StandardDirection sdoDirectionAppointChildrenGuardian;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-OBJECT_TO_REQUEST_FOR_DISCLOSURE")
    StandardDirection sdoDirectionObjectToRequestForDisclosure;

    @JsonDeserialize(using = StandardDirectionDeserializer.class)
    @JsonProperty("sdoDirection-ARRANGE_INTERPRETERS")
    StandardDirection sdoDirectionArrangeInterpreters;


    public JudgeAndLegalAdvisor getGatekeepingOrderIssuingJudge() {
        return defaultIfNull(gatekeepingOrderIssuingJudge, JudgeAndLegalAdvisor.builder().build());
    }

    public SaveOrSendGatekeepingOrder getSaveOrSendGatekeepingOrder() {
        return defaultIfNull(saveOrSendGatekeepingOrder, SaveOrSendGatekeepingOrder.builder().build());
    }

    public List<DirectionType> getRequestedDirections() {
        return Stream.of(sdoDirectionsForAll, sdoDirectionsForLocalAuthority, sdoDirectionsForRespondents,
            sdoDirectionsForCafcass, sdoDirectionsForOthers, sdoDirectionsForCourt)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    public static String[] temporaryFields() {
        return getFieldsListWithAnnotation(GatekeepingOrderEventData.class, Temp.class).stream()
            .map(Field::getName)
            .toArray(String[]::new);
    }

    public List<Element<StandardDirection>> resetStandardDirections() {
        this.standardDirections = new ArrayList<>();
        return standardDirections;
    }
}
