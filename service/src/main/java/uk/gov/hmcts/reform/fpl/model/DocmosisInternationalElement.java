package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DocmosisInternationalElement {
    private final String possibleCarer;
    private final String significantEvents;
    private final String proceedings;
    private final String internationalAuthorityInvolvement;
    private final String issues;
}
