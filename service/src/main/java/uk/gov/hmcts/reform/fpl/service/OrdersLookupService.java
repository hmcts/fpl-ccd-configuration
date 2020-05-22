package uk.gov.hmcts.reform.fpl.service;

import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;

public interface OrdersLookupService {
    OrderDefinition getStandardDirectionOrder();
}
