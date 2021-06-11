package uk.gov.hmcts.reform.fpl.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;

import static java.lang.Integer.valueOf;
import static java.lang.Math.abs;

@Data
@Builder
@AllArgsConstructor
public class DirectionConfiguration {
    private final DirectionType id;
    private final String title;
    private final String text;
    private final DirectionAssignee assignee;
    private final Display display;

    public StandardDirection create() {
        return StandardDirection.builder()
            .type(id)
            .title(title)
            .assignee(assignee)
            .showDateOnly(YesNo.from(display.isShowDateOnly()))
            .daysBeforeHearing(abs(valueOf(display.getDelta())))
            .description(text)
            .build();
    }
}
