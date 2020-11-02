package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentativesChange;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LegalRepresentativesDifferenceCalculator {

    public LegalRepresentativesChange calculate(List<LegalRepresentative> original,
                                                List<LegalRepresentative> updated) {

        return LegalRepresentativesChange.builder()
            .added(toLegalRepresentatives(
                Sets.difference(
                    toRelevantSet(updated),
                    toRelevantSet(original)
                )))
            .removed(toLegalRepresentatives(
                Sets.difference(
                    toRelevantSet(original),
                    toRelevantSet(updated))
            ))
            .build();
    }

    private Set<RelevantUniqueInformation> toRelevantSet(List<LegalRepresentative> original) {
        return original.stream()
            .map(this::toRelevantInformation)
            .collect(Collectors.toSet());
    }

    private Set<LegalRepresentative> toLegalRepresentatives(Set<RelevantUniqueInformation> uniqueInformation) {
        return uniqueInformation.stream()
            .map(RelevantUniqueInformation::getLegalRepresentative)
            .collect(Collectors.toSet());
    }

    private RelevantUniqueInformation toRelevantInformation(LegalRepresentative legalRepresentative) {
        return new RelevantUniqueInformation(legalRepresentative.getEmail(), legalRepresentative);
    }

    @Value
    private class RelevantUniqueInformation {
        String email;
        @EqualsAndHashCode.Exclude
        LegalRepresentative legalRepresentative;
    }
}
