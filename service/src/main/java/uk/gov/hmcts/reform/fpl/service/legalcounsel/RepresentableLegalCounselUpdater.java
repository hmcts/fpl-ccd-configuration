package uk.gov.hmcts.reform.fpl.service.legalcounsel;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;

@Component
public class RepresentableLegalCounselUpdater {

    public <R extends WithSolicitor, O extends WithSolicitor> List<Element<R>> updateLegalCounselForRemovedSolicitors(
        List<Element<R>> oldRepresentables,
        List<Element<R>> currentRepresentables,
        List<Element<O>> otherRepresentablesToQuery) {

        final List<Element<? extends WithSolicitor>> representablesToQuery = Stream.concat(
            oldRepresentables.stream(), otherRepresentablesToQuery.stream()
        ).collect(toUnmodifiableList());

        for (int i = 0; i < oldRepresentables.size(); i++) {
            Optional<String> oldOrgId = Optional.ofNullable(oldRepresentables.get(i))
                .map(Element::getValue)
                .flatMap(this::getOrgId);

            Optional<WithSolicitor> currentRepresentable = Optional.ofNullable(currentRepresentables.get(i))
                .map(Element::getValue);

            Optional<String> currentOrgId = currentRepresentable.flatMap(this::getOrgId);

            if (!Objects.equals(oldOrgId, currentOrgId)) {
                // The organisation has changed
                // We need to see if anyone else has the same org to pull some legal counsellors
                List<Element<LegalCounsellor>> legalCounsel = currentOrgId.map(
                    orgId -> legalCounselByOrganisation(orgId, representablesToQuery)
                ).orElse(Collections.emptyList());

                currentRepresentable.ifPresent(representable -> representable.setLegalCounsellors(legalCounsel));
            }
        }

        return currentRepresentables;
    }

    private List<Element<LegalCounsellor>> legalCounselByOrganisation(String organisationId,
                                                                      List<Element<? extends WithSolicitor>> toQuery) {
        return toQuery.stream()
            .map(Element::getValue)
            .filter(respondent -> Optional.ofNullable(respondent)
                .flatMap(this::getOrgId)
                .map(organisationId::equals)
                .orElse(false)
            )
            .findFirst()
            .map(WithSolicitor::getLegalCounsellors)
            .orElse(null);
    }

    private Optional<String> getOrgId(WithSolicitor representable) {
        return Optional.ofNullable(representable)
            .map(WithSolicitor::getSolicitor)
            .map(RespondentSolicitor::getOrganisation)
            .map(Organisation::getOrganisationID);
    }
}
