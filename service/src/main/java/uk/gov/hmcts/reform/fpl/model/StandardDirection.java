package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.DueDateType;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;

import java.time.LocalDateTime;

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
}
