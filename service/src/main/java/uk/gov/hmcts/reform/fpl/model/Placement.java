package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Stream.ofNullable;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.PLACEMENT_ORDER;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Placement {

    @JsonProperty("placementChildId")
    private UUID childId;

    @JsonProperty("placementChildName")
    private String childName;

    @JsonProperty("placementApplication")
    public DocumentReference application;

    @JsonProperty("placementSupportingDocuments")
    private List<Element<PlacementSupportingDocument>> supportingDocuments;

    @JsonProperty("placementConfidentialDocuments")
    private List<Element<PlacementConfidentialDocument>> confidentialDocuments;

    @JsonProperty("placementOrderAndNotices")
    private List<Element<PlacementOrderAndNotices>> orderAndNotices;

    @JsonIgnore
    public Placement setChild(Element<Child> child) {
        this.setChildId(child.getId());
        this.setChildName(child.getValue().getParty().getFullName());
        return this;
    }

    @JsonIgnore
    public Placement removePlacementOrder() {
        List<Element<PlacementOrderAndNotices>> filteredOrders = ofNullable(this.getOrderAndNotices())
            .flatMap(Collection::stream)
            .filter(x -> x.getValue().getType() != PLACEMENT_ORDER)
            .collect(Collectors.toList());

        return this.toBuilder().orderAndNotices(filteredOrders.isEmpty() ? null : filteredOrders).build();
    }

    @JsonIgnore
    public Placement removeConfidentialDocuments() {
        return this.toBuilder().confidentialDocuments(null).build();
    }
}
