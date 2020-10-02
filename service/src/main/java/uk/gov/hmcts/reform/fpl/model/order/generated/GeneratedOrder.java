package uk.gov.hmcts.reform.fpl.model.order.generated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.json.converter.BasicChildConverter;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

@Data
@Builder(toBuilder = true)
public class GeneratedOrder {
    private final String type;
    private final String title;
    private final String details;
    private final DocumentReference document;
    private final String dateOfIssue;
    private final String date;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final FurtherDirections furtherDirections;
    private final String expiryDate;
    private final String courtName;
    private final String uploader;
    @JsonSerialize(contentConverter = BasicChildConverter.class)
    private final List<Element<Child>> children;
    private String removalReason;

    @JsonIgnore
    public boolean isRemovable() {
        return GeneratedOrderType.fromType(type).isRemovable();
    }

    public String asLabel() {
        return defaultIfEmpty(title, type) + " - " + dateOfIssue;
    }
}
