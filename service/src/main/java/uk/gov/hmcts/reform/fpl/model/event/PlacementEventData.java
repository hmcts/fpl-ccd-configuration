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

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlacementEventData {

    public static final String PLACEMENT_GROUP = "Placement";
    public static final String HEARING_GROUP = "Hearing";

    @Temp
    private Cardinality placementChildrenCardinality;

    @Temp
    @FieldsGroup(PLACEMENT_GROUP)
    private String placementChildName;

    @Temp
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private DynamicList placementChildrenList;

    @Temp
    @FieldsGroup(PLACEMENT_GROUP)
    private Placement placement;

    @Temp
    private String placementFee;

    @TempNullify
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo placementPaymentRequired;

    @TempNullify
    private PBAPayment placementPayment;

    private LocalDateTime placementLastPaymentTime;

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

    @Temp
    @FieldsGroup(HEARING_GROUP)
    private DocumentReference placementNotice;

    @Temp
    @FieldsGroup(HEARING_GROUP)
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo hasExistingPlacements;

    @Temp
    @FieldsGroup(HEARING_GROUP)
    private LocalDateTime placementNoticeDateTime;

    @Temp
    @FieldsGroup(HEARING_GROUP)
    private String placementNoticeDuration;

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

    private UUID placementIdToBeSealed;
}
