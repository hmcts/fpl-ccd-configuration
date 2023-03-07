package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;

import java.time.LocalDateTime;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.APPOINT_CHILDREN_GUARDIAN_IMMEDIATE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.ARRANGE_INTERPRETERS_IMMEDIATE;

@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
public class StandardDirection {
    private final String title;
    private final String description;
    private final DirectionType type;
    private final DirectionAssignee assignee;
    private LocalDateTime dateToBeCompletedBy;
    private Integer daysBeforeHearing;
    private final DirectionDueDateType dueDateType;
    private static final Integer DEFAULT_DAYS_BEFORE_HEARING = 2;

    @JsonIgnore
    public StandardDirection applyConfig(DirectionConfiguration config) {
        return this.toBuilder()
            .type(config.getType())
            .title(config.getTitle())
            .assignee(config.getAssignee())
            .daysBeforeHearing(
                isImmediateStandardDirection(config.getType())
                    ? null
                    : defaultIfNull(daysBeforeHearing, DEFAULT_DAYS_BEFORE_HEARING)
            )
            .description(defaultIfNull(description, config.getText()))
            .build();
    }

    @JsonIgnore
    public boolean isImmediateStandardDirection(DirectionType type) {
        return APPOINT_CHILDREN_GUARDIAN_IMMEDIATE.equals(type) || ARRANGE_INTERPRETERS_IMMEDIATE.equals(type);
    }
}
