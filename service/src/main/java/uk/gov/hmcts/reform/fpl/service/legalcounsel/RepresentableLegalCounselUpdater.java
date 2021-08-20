package uk.gov.hmcts.reform.fpl.service.legalcounsel;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RepresentableLegalCounselUpdater {

    private final OrganisationService orgService;
    private final UserService user;

    public Set<LegalCounsellorRemoved> buildEventsForAccessRemoval(CaseData caseData, CaseData caseDataBefore,
                                                                   final Representing representing) {
        // need to find the representables that have been updated
        List<WithSolicitor> current = unwrapElements(representing.getTarget().apply(caseData));
        List<WithSolicitor> old = unwrapElements(representing.getTarget().apply(caseDataBefore));

        Set<LegalCounsellor> allCurrentLegalCounsellors = Arrays.stream(Representing.values())
            .flatMap(representingType -> unwrapElements(representingType.getTarget().apply(caseData)).stream())
            .flatMap(represented -> unwrapElements(represented.getLegalCounsellors()).stream())
            .collect(Collectors.toSet());

        Set<LegalCounsellorRemoved> events = new HashSet<>();

        for (int i = 0; i < old.size(); i++) {
            WithSolicitor currentRepresentable = current.get(i);
            WithSolicitor oldRepresentable = old.get(i);
            if (!Objects.equals(currentRepresentable.getSolicitor(), oldRepresentable.getSolicitor())) {
                String orgName = getOrgName(currentRepresentable);
                events.addAll(unwrapElements(oldRepresentable.getLegalCounsellors()).stream()
                    .filter(not(allCurrentLegalCounsellors::contains))
                    .map(counsellor -> new LegalCounsellorRemoved(caseData, orgName, counsellor))
                    .collect(Collectors.toSet()));
            }
        }

        return events;
    }

    public Map<String, Object> updateLegalCounselFromNoC(CaseData caseData, CaseData caseDataBefore) {
        ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();
        SolicitorRole role = SolicitorRole.from(change.getCaseRoleId().getValueCode()).orElseThrow();

        Representing representing = role.getRepresenting();
        Representing opposite = Representing.CHILD == representing ? Representing.RESPONDENT : Representing.CHILD;

        Function<CaseData, List<Element<WithSolicitor>>> toUpdateTarget = representing.getTarget();
        Function<CaseData, List<Element<WithSolicitor>>> toQueryTarget = opposite.getTarget();

        return Map.of(
            representing.getCaseField(), updateLegalCounsel(
                toUpdateTarget.apply(caseDataBefore), toUpdateTarget.apply(caseData), toQueryTarget.apply(caseData)
            )
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

    private String getOrgName(WithSolicitor changed) {
        return getOrgId(changed)
            .flatMap(orgService::findOrganisation)
            .map(uk.gov.hmcts.reform.rd.model.Organisation::getName)
            .orElseGet(() -> {
                RespondentSolicitor solicitor = changed.getSolicitor();
                if (null != solicitor && null != solicitor.getUnregisteredOrganisation()) {
                    return solicitor.getUnregisteredOrganisation().getName();
                }
                // in the scenario that a user has removed the current solicitor (possible in respondent flow)
                // Refer to all HMCTS staff as just HMCTS to cover the scenario that it is an admin performing the
                // action (possibly on behalf of a judge)
                // otherwise it must be a solicitor, and they must be associated to organisation
                if (user.isHmctsUser()) {
                    return "HMCTS";
                }
                return orgService.findOrganisation()
                    .map(uk.gov.hmcts.reform.rd.model.Organisation::getName)
                    .orElseThrow(() -> new IllegalStateException(
                        "Solicitor was changed to null, the user was not HMCTS, the user does not have a organisation"
                        + " associated to them"
                    ));
            });
    }
}
