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
import uk.gov.hmcts.reform.fpl.model.interfaces.SelectableItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Placement implements SelectableItem {

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

    @JsonProperty("placementNoticeDocumentsRemoved")
    private List<Element<PlacementNoticeDocument>> noticeDocumentsRemoved;

    @JsonProperty("placementUploadDateTime")
    public LocalDateTime placementUploadDateTime;

    @JsonProperty("placementNotice")
    private DocumentReference placementNotice;

    @JsonProperty("placementRespondentsToNotify")
    private List<Element<Respondent>> placementRespondentsToNotify;

    @JsonIgnore
    public Placement nonConfidential(boolean withNoticesResponses) {

        final PlacementBuilder placementBuilder = this.toBuilder().confidentialDocuments(null);

        if (!withNoticesResponses && nonNull(noticeDocuments)) {
            final List<Element<PlacementNoticeDocument>> nonConfidentialNotices = noticeDocuments.stream()
                .map(notice -> element(notice.getId(), notice.getValue().toBuilder()
                    .response(null)
                    .responseDescription(null)
                    .build()))
                .collect(toList());

            placementBuilder.noticeDocuments(nonConfidentialNotices);
        }

        return placementBuilder.build();
    }

    @JsonIgnore
    public DocumentReference getPlacementApplicationCopy() {
        return application;
    }

    @JsonProperty("isSubmitted")
    @JsonDeserialize(using = YesNoDeserializer.class)
    public YesNo isSubmitted() {
        return YesNo.from(nonNull(this.placementUploadDateTime));
    }

    @Override
    @JsonIgnore
    public String toLabel() {
        if (isNull(placementUploadDateTime)) {
            return format("A50, %s", childName);
        }
        return format("A50, %s, %s", childName, getUploadedDateTime());
    }

    @Override
    @JsonIgnore
    public int getSortOrder() {
        return 3;
    }

    @Override
    @JsonIgnore
    public String getUploadedDateTime() {
        return ofNullable(placementUploadDateTime)
            .map(time -> formatLocalDateTimeBaseUsingFormat(time, DATE_TIME))
            .orElse(null);
    }
}
