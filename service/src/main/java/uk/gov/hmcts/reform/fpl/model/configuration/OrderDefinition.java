package uk.gov.hmcts.reform.fpl.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class OrderDefinition {
    // I wanted to use OrderType but it's already something else, what can we use here
    private final String orderType;
    private final Language language;
    private final String service;
    private final List<DirectionConfiguration> directions;
}
