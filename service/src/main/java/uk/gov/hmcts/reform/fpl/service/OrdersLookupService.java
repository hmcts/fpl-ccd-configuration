package uk.gov.hmcts.reform.fpl.service;

import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;

import java.io.IOException;
import java.util.List;

public interface OrdersLookupService {
    OrderDefinition getStandardDirectionOrder() throws IOException;

    List<Element<Direction>> getStandardDirections(HearingBooking hearingBooking) throws IOException;
}
