package uk.gov.hmcts.reform.fpl.service.legalcounsel;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RepresentableLegalCounselUpdater {

    private final OrganisationService orgService;
    private final ManageLegalCounselService manageService;

    public List<LegalCounsellorRemoved> buildEventsForAccessRemoval(CaseData caseData, CaseData caseDataBefore,
                                                                    final SolicitorRole.Representing representing) {
        // need to find the case role that has been updated
        List<Element<WithSolicitor>> current = representing.getTarget().apply(caseData);
        List<Element<WithSolicitor>> old = representing.getTarget().apply(caseDataBefore);

        WithSolicitor changed = null;
        int index = -1;

        for (int i = 0; i < current.size(); i++) {
            WithSolicitor representable = current.get(i).getValue();
            if (!Objects.equals(representable.getSolicitor(), old.get(i).getValue().getSolicitor())) {
                // only need the first role as the retrieval method ignores all others
                changed = representable;
                index = i;
                break;
            }
        }

        // wrap in list for retrieveLegalCounselForRoles method
        List<SolicitorRole> roles = SolicitorRole.from(index, representing).map(List::of).orElseThrow();

        String orgName = getOrgId(changed)
            .map(orgService::findOrganisation)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(uk.gov.hmcts.reform.rd.model.Organisation::getName)
            .orElseThrow();

        List<LegalCounsellor> currentLegalCounsellors = manageService.retrieveLegalCounselForRoles(caseData, roles)
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        return manageService.retrieveLegalCounselForRoles(caseDataBefore, roles)
            .stream()
            .map(Element::getValue)
            .filter(not(currentLegalCounsellors::contains))
            .map(counsellor -> orgService.findUserByEmail(counsellor.getEmail()).map(id -> Pair.of(id, counsellor)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(pair -> new LegalCounsellorRemoved(caseData, orgName, pair))
            .collect(Collectors.toList());
    }

    public Map<String, Object> updateLegalCounsel(CaseData caseData, CaseData caseDataBefore) {
        // make mutable and immutable copies of the current collections
        List<Element<Respondent>> currentRespondents = new ArrayList<>(caseData.getAllRespondents());
        List<Element<Respondent>> currentRespondentsCopy = List.copyOf(currentRespondents);
        List<Element<Respondent>> oldRespondents = caseDataBefore.getAllRespondents();

        List<Element<Child>> currentChildren = new ArrayList<>(caseData.getAllChildren());
        List<Element<Child>> currentChildrenCopy = List.copyOf(currentChildren);
        List<Element<Child>> oldChildren = caseDataBefore.getAllChildren();

        currentRespondents.removeAll(oldRespondents);
        currentChildren.removeAll(oldChildren);

        List<Element<Respondent>> updatedRespondents = currentRespondentsCopy;
        if (!currentRespondents.isEmpty()) {
            updatedRespondents = updateLegalCounsel(oldRespondents, currentRespondentsCopy, currentChildrenCopy);
        }

        List<Element<Child>> updatedChildren = currentChildrenCopy;
        if (!currentChildren.isEmpty()) {
            updatedChildren = updateLegalCounsel(oldChildren, currentChildrenCopy, currentRespondentsCopy);
        }

        return Map.of(
            "respondents1", updatedRespondents,
            "children1", updatedChildren
        );
    }

    public <R extends WithSolicitor, O extends WithSolicitor> List<Element<R>> updateLegalCounsel(
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
