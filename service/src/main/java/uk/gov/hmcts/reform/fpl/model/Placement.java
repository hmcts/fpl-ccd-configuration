package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.json.deserializer.YesNoDeserializer;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.nonNull;

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

    @JsonProperty("placementNoticeDocuments")
    private List<Element<PlacementNoticeDocument>> noticeDocuments;

    @JsonProperty("placementUploadDateTime")
    public LocalDateTime placementUploadDateTime;

    @JsonIgnore
    public Placement nonConfidential() {
        return this.toBuilder().confidentialDocuments(null).build();
    }

    public DocumentReference getPlacementApplicationCopy() {
        return application;
    }

    @JsonProperty("isSubmitted")
    @JsonDeserialize(using = YesNoDeserializer.class)
    public YesNo isSubmitted() {
        return YesNo.from(nonNull(this.placementUploadDateTime));
    }
}
