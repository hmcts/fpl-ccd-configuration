package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Solicitor {
    private final String dx;
    private final String name;
    private final String email;
    private final String mobile;
    private final String reference;
    private final String telephone;
}
