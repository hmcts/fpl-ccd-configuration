package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Builder
@Value
public class LegalRepresentativesChange {
    private final Set<LegalRepresentative> added;
    private final Set<LegalRepresentative> removed;
}
