package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;

@Data
public class Scenario {
    private String name;
    private String description;
    private Request request;
    private Expectation expectation;
}
