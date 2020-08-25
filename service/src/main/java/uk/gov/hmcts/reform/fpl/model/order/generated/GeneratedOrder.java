package uk.gov.hmcts.reform.fpl.model.order.generated;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.json.converter.BasicChildConverter;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.RemovableOrder;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
public class GeneratedOrder extends RemovableOrder {
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
    @JsonSerialize(contentConverter = BasicChildConverter.class)
    private final List<Element<Child>> children;

    @Override
    public String asLabel() {
        return defaultIfNull(title, type) + " - " + dateOfIssue;
    }
}
