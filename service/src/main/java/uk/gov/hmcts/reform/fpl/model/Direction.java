package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Direction {
    private final UUID id;
    private final String type;
    private  String text;
    private final String status;
    private final String assignee;
    private String readOnly;
    private LocalDateTime completeBy;
}
