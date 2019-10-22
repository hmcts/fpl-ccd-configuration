package uk.gov.hmcts.reform.fpl.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;

@Data
@Builder
@AllArgsConstructor
public class DirectionConfiguration {
    private final String title;
    private final String text;
    private final DirectionAssignee assignee;
    private final Display display;
}
