package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.enums.DueDateType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;

import java.time.LocalDateTime;

import static java.lang.Integer.valueOf;
import static java.lang.Math.abs;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
public class StandardDirection {
    private final String title;
    private final String description;
    private final DirectionType type;
    private final DirectionAssignee assignee;
    private final LocalDateTime dateToBeCompletedBy;
    private final Integer daysBeforeHearing;
    private final YesNo showDateOnly;
    private final DueDateType dueDateType;

    public StandardDirection apply(DirectionConfiguration config) {
        return this.toBuilder()
            .type(config.getId())
            .title(config.getTitle())
            .assignee(config.getAssignee())
            .showDateOnly(YesNo.from(config.getDisplay().isShowDateOnly()))
            .daysBeforeHearing(defaultIfNull(daysBeforeHearing, abs(valueOf(config.getDisplay().getDelta()))))
            .description(defaultIfNull(description, config.getText()))
            .build();
    }
}
