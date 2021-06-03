package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    List<DirectionType> sdoDirectionsForAll;
    List<DirectionType> sdoDirectionsForLocalAuthority;
    List<DirectionType> sdoDirectionsForRespondents;
    List<DirectionType> sdoDirectionsForCafcass;
    List<DirectionType> sdoDirectionsForOthers;
    List<DirectionType> sdoDirectionsForCourt;


    @JsonProperty("sdoDirection-REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE")
    StandardDirection sdoDirectionRequestPermissionForExpertEvidence;
    @JsonProperty("sdoDirection-REQUEST_HELP_TO_TAKE_PART_IN_PROCEEDINGS")
    StandardDirection sdoDirectionRequestHelpToTakePartInProceedeings;
    @JsonProperty("sdoDirection-ASK_FOR_DISCLOSURE")
    StandardDirection sdoDirectionAskForDisclousure;
    @JsonProperty("sdoDirection-ATTEND_HEARING")
    StandardDirection sdoDirectionAttendHearing;
    @JsonProperty("sdoDirection-CONTACT_ALTERNATIVE_CARERS")
    StandardDirection sdoDirectionContactAlternativeCarers;
    @JsonProperty("sdoDirection-SEND_DOCUMENTS_TO_ALL_PARTIES")
    StandardDirection sdoDirectionSendDocumentsToAllParties;
    @JsonProperty("sdoDirection-SEND_MISSING_ANNEX")
    StandardDirection sdoDirectionSendMissingAnnex;
    @JsonProperty("sdoDirection-IDENTIFY_ALTERNATIVE_CARERS")
    StandardDirection sdoDirectionIdentifyAlternativeCarers;
    @JsonProperty("sdoDirection-SEND_TRANSLATED_DOCUMENTS")
    StandardDirection sdoDirectionSendTransaletedDocuments;
    @JsonProperty("sdoDirection-LODGE_BUNDLE")
    StandardDirection sdoDirectionLodgeBundle;
    @JsonProperty("sdoDirection-SEND_CASE_SUMMARY")
    StandardDirection sdoDirectionSendCaseSummary;
    @JsonProperty("sdoDirection-CONSIDER_JURISDICTION")
    StandardDirection sdoDirectionConsiderJurisdiction;
    @JsonProperty("sdoDirection-SEND_RESPONSE_TO_THRESHOLD_STATEMENT")
    StandardDirection sdoDirectionSendResponseToThresholdStatement;
    @JsonProperty("sdoDirection-ARRANGE_ADVOCATES_MEETING")
    StandardDirection sdoDirectionArrangeAdvocatesMeeting;
    @JsonProperty("sdoDirection-SEND_GUARDIANS_ANALYSIS")
    StandardDirection sdoDirectionSendGuardianAnalysis;
    @JsonProperty("sdoDirection-APPOINT_CHILDREN_GUARDIAN")
    StandardDirection sdoDirectionAppointChildrenGuardian;
    @JsonProperty("sdoDirection-OBJECT_TO_REQUEST_FOR_DISCLOSURE")
    StandardDirection sdoDirectionObjectToRequestForDisclosure;
    @JsonProperty("sdoDirection-ARRANGE_INTERPRETERS")
    StandardDirection sdoDirectionArrangeInterpreters;


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
}
