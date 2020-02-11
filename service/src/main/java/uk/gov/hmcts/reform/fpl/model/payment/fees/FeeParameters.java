package uk.gov.hmcts.reform.fpl.model.payment.fees;

import lombok.Data;

@Data
public class FeeParameters {
    private final String service;
    private final String jurisdiction1;
    private final String jurisdiction2;
    private final String channel;
    private final String event;
    private final String keyword;
}
