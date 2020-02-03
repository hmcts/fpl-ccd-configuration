package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

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

    @JsonProperty("placementOrderAndNotices")
    private List<Element<PlacementOrderAndNotices>> orderAndNotices;

    @JsonIgnore
    public Placement setChild(Element<Child> child) {
        this.setChildId(child.getId());
        this.setChildName(child.getValue().getParty().getFullName());
        return this;
    }
}
