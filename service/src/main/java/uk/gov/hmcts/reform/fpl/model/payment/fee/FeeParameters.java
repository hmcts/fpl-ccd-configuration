package uk.gov.hmcts.reform.fpl.model.payment.fee;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeeParameters {
    private final String channel;
    private final String event;
    private final String jurisdiction1;
    private final String jurisdiction2;
    private final String keyword;
    private final String service;
}
