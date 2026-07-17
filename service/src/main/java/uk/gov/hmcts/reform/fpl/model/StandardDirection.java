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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
public class StandardDirection {
    @CCD(
            label = "Type",
            hint = "You must include a brief direction summary",
            showCondition = "dueDateType = \"DO NOT SHOW\""
    )
    private final String title;
    @CCD(label = "Description", showCondition = "dueDateType = \"DO NOT SHOW\"", typeOverride = FieldType.TextArea)
    private final String description;
    @CCD(label = "Type", showCondition = "dueDateType = \"DO NOT SHOW\"", typeOverride = FieldType.Text)
    private final DirectionType type;
    @CCD(
            label = "Type",
            hint = "You must include a brief direction summary",
            showCondition = "dueDateType = \"DO NOT SHOW\"",
            typeOverride = FieldType.Text
    )
    private final DirectionAssignee assignee;
    @CCD(label = "Date and time", hint = "Use 24h format", showCondition = "dueDateType=\"DATE\"")
    private LocalDateTime dateToBeCompletedBy;
    @CCD(label = "Number of days", showCondition = "dueDateType=\"DAYS\"", min = 0, max = 365)
    private Integer daysBeforeHearing;
    @CCD(label = "Due date")
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
