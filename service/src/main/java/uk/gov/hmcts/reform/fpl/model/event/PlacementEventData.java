package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.Cardinality;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.json.deserializer.YesNoDeserializer;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.TempNullify;
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

    @Temp
    private Cardinality placementChildrenCardinality;

    @Temp
    private String placementChildName;

    @Temp
    private DynamicList placementChildrenList;

    @Temp
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

    public List<Element<Placement>> getPlacementsNonConfidential() {
        if (isEmpty(placements)) {
            return emptyList();
        }
        return placements.stream()
            .map(element -> element(element.getId(), element.getValue().nonConfidential()))
            .collect(toList());
    }

    public void setPlacement(Placement placement) {
        this.placement = placement;
        this.placementChildName = ofNullable(placement).map(Placement::getChildName).orElse(null);
    }
}
