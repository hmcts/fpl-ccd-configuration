package uk.gov.hmcts.reform.fpl.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class OrderDefinition {
    private final String type;
    private final Language language;
    private final String service;
    private final List<DirectionConfiguration> standardDirections;
    private final DirectionConfiguration customDirection;
}
