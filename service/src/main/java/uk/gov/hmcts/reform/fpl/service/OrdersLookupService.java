package uk.gov.hmcts.reform.fpl.service;

import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;

import java.io.IOException;

public interface OrdersLookupService {
    OrderDefinition getDirectionOrder() throws IOException;
}
