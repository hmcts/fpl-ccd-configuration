package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Builder
@Value
public class LegalRepresentativesChange {
    Set<LegalRepresentative> added;
    Set<LegalRepresentative> removed;
}
