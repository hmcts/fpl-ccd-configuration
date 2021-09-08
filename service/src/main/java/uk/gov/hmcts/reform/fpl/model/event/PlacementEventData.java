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
import uk.gov.hmcts.reform.fpl.model.TempNullable;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
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

    @TempNullable
    @JsonDeserialize(using = YesNoDeserializer.class)
    private YesNo placementPaymentRequired;

    @TempNullable
    private PBAPayment placementPayment;

    private LocalDateTime placementLastPaymentTime;

    @Builder.Default
    private List<Element<Placement>> placements = new ArrayList<>();

    public List<Element<Placement>> getPlacementsNonConfidential() {
        return placements.stream()
            .map(element -> element(element.getId(), element.getValue().nonConfidential()))
            .collect(toList());
    }

}
