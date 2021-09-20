package uk.gov.hmcts.reform.fpl.service.orders.generator.common;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class OrderDetailsWithEndTypeMessages {

    String messageWithSpecifiedTime;
    String messageWithNumberOfMonths;
    String messageWithEndOfProceedings;

}
