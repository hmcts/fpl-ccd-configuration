package uk.gov.hmcts.reform.fpl.model.order.generated;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.util.List;

import static java.util.Collections.emptyList;

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
    private final List<Element<Child>> children;

    public List<Element<Child>> getChildren() {
        return ObjectUtils.defaultIfNull(children, emptyList());
    }
}
