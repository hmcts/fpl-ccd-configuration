package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.Cardinality;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.json.deserializer.DynamicListDeserializer;
import uk.gov.hmcts.reform.fpl.json.deserializer.YesNoDeserializer;
import uk.gov.hmcts.reform.fpl.model.FieldsGroup;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.TempNullify;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCrudPlus2RolesThgnehAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcassCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateCudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LASOLICITORCaseworkerPubliclawCourtadminCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGCaseworkerPubliclawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CAFCASSSOLICITORUPlus24RolesYmydudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGCrPlus6RolesBwjkinAccess;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlacementEventData {

    public static final String PLACEMENT_GROUP = "Placement";
    public static final String HEARING_GROUP = "Hearing";

    @CCD(
            label = "Children cardinality",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "Cardinality",
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, LABARRISTERCrudPlus2RolesThgnehAccess.class}
    )
    @Temp
    private Cardinality placementChildrenCardinality;

    @CCD(
            label = "Child name",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, LABARRISTERCrudPlus2RolesThgnehAccess.class}
    )
    @Temp
    @FieldsGroup(PLACEMENT_GROUP)
    private String placementChildName;

    @CCD(
            label = "Which child?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, LABARRISTERCrudPlus2RolesThgnehAccess.class}
    )
    @Temp
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private DynamicList placementChildrenList;

    @CCD(
            label = "Placement application",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, LABARRISTERCrudPlus2RolesThgnehAccess.class, CaseworkerPubliclawCafcassCrudAccess.class}
    )
    @Temp
    @FieldsGroup(PLACEMENT_GROUP)
    private Placement placement;

    @CCD(
            label = "Application fee to pay",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, LABARRISTERCrudPlus2RolesThgnehAccess.class}
    )
    @Temp
    private String placementFee;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, LABARRISTERCrudPlus2RolesThgnehAccess.class}
    )
    @TempNullify
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo placementPaymentRequired;

    @CCD(
            label = "PBA Payment",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, LABARRISTERCrudAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class, CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    @TempNullify
    private PBAPayment placementPayment;

    @CCD(
            label = " ",
            searchable = false,
            access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class, EPSMANAGINGLAMANAGINGCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    private LocalDateTime placementLastPaymentTime;

    @CCD(label = "Child", searchable = false, access = {CAFCASSSOLICITORUPlus24RolesYmydudAccess.class})
    @Builder.Default
    private List<Element<Placement>> placements = new ArrayList<>();

    public List<Element<Placement>> getPlacementsNonConfidential(boolean withNoticesResponses) {
        if (isEmpty(placements)) {
            return emptyList();
        }
        return placements.stream()
                .map(element -> element(element.getId(), element.getValue().nonConfidential(withNoticesResponses)))
                .collect(toList());
    }

    public List<Element<Placement>> getPlacementsNonConfidentialWithNotices(boolean withNoticesResponses) {
        if (isEmpty(placements)) {
            return emptyList();
        }

        return placements.stream()
                .filter(element -> element.getValue().getPlacementNotice() != null)
                .filter(element -> !element.getValue().getPlacementNotice().isEmpty())
                .map(element -> element(element.getId(), element.getValue().nonConfidential(withNoticesResponses)))
                .toList();
    }

    public void setPlacement(Placement placement) {
        this.placement = placement;
        this.placementChildName = ofNullable(placement).map(Placement::getChildName).orElse(null);
    }

    @CCD(
            label = "Notice of hearing for placement",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    @FieldsGroup(HEARING_GROUP)
    private DocumentReference placementNotice;

    @CCD(
            label = "Has existing placements?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawCourtadminCrAccess.class}
    )
    @Temp
    @FieldsGroup(HEARING_GROUP)
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo hasExistingPlacements;

    @CCD(
            label = "Date and time of the hearing",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    @FieldsGroup(HEARING_GROUP)
    private LocalDateTime placementNoticeDateTime;

    @CCD(
            label = "Hearing duration (hours)",
            searchable = false,
            typeOverride = FieldType.Number,
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    @FieldsGroup(HEARING_GROUP)
    private String placementNoticeDuration;

    @CCD(
            label = "Hearing venue",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "HearingVenue",
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    @FieldsGroup(HEARING_GROUP)
    private final String placementNoticeVenue;

    @Temp
    @FieldsGroup(HEARING_GROUP)
    private List<Element<Respondent>> placementRespondentsToNotify;

    @Temp
    @FieldsGroup(HEARING_GROUP)
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo sendPlacementNoticeToAllRespondents;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {EPSMANAGINGCrPlus6RolesBwjkinAccess.class}
    )
    private UUID placementIdToBeSealed;
}
