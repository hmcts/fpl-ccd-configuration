package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joda.time.DateTime;

@Data
@Builder(toBuilder = true)
public class CustomDirection {
    private String id;
    private String title;
    private String description;
    private String assignee;
    private DateTime dateToBeCompletedBy;
}
