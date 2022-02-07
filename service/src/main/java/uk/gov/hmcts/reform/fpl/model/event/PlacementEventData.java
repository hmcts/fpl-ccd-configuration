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

    public static final String NOTICE_GROUP = "Notice";
    public static final String PLACEMENT_GROUP = "Placement";

    @Temp
    private Cardinality placementChildrenCardinality;

    @Temp
    @FieldsGroup(PLACEMENT_GROUP)
    private String placementChildName;

    @Temp
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private DynamicList placementChildrenList;

    @TempNullify
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

    public void setPlacement(Placement placement) {
        this.placement = placement;
        this.placementChildName = ofNullable(placement).map(Placement::getChildName).orElse(null);
    }

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo placementNoticeForLocalAuthorityRequired;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private DocumentReference placementNoticeForLocalAuthority;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private String placementNoticeForLocalAuthorityDescription;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo placementNoticeResponseFromLocalAuthorityReceived;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private DocumentReference placementNoticeResponseFromLocalAuthority;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private String placementNoticeResponseFromLocalAuthorityDescription;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo placementNoticeForCafcassRequired;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private DocumentReference placementNoticeForCafcass;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private String placementNoticeForCafcassDescription;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo placementNoticeResponseFromCafcassReceived;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private DocumentReference placementNoticeResponseFromCafcass;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private String placementNoticeResponseFromCafcassDescription;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo placementNoticeForFirstParentRequired;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private DocumentReference placementNoticeForFirstParent;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private String placementNoticeForFirstParentDescription;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private DynamicList placementNoticeForFirstParentParentsList;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo placementNoticeResponseFromFirstParentReceived;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private DocumentReference placementNoticeResponseFromFirstParent;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private String placementNoticeResponseFromFirstParentDescription;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo placementNoticeForSecondParentRequired;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private DocumentReference placementNoticeForSecondParent;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private String placementNoticeForSecondParentDescription;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private DynamicList placementNoticeForSecondParentParentsList;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo placementNoticeResponseFromSecondParentReceived;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private DocumentReference placementNoticeResponseFromSecondParent;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private String placementNoticeResponseFromSecondParentDescription;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    private List<Element<Respondent>> placementRespondentsToNotify;

    @Temp
    @FieldsGroup(NOTICE_GROUP)
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo sendPlacementNoticeToAllRespondents;
}
