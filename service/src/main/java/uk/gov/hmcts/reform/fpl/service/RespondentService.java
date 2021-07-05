package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentService {

    private final Time time;

    public String buildRespondentLabel(List<Element<Respondent>> respondents) {
        StringBuilder sb = new StringBuilder();

        if (isNotEmpty(respondents)) {
            for (int i = 0; i < respondents.size(); i++) {
                RespondentParty respondentParty = respondents.get(i).getValue().getParty();

                sb.append(String.format("Respondent %d - %s", i + 1, getRespondentFullName(respondentParty)))
                    .append("\n");
            }
        } else {
            sb.append("No respondents on the case");
        }

        return sb.toString();
    }

    public List<Element<Respondent>> persistRepresentativesRelationship(List<Element<Respondent>> newRespondents,
                                                                        List<Element<Respondent>> oldRespondents) {
        oldRespondents.forEach(respondentElement -> {
            Optional<Element<Respondent>> respondentOptional = findElement(respondentElement.getId(), newRespondents);
            respondentOptional.ifPresent(respondent ->
                respondent.getValue().setRepresentedBy(respondentElement.getValue().getRepresentedBy())
            );
        });

        return newRespondents;
    }

    //If user entered details that were subsequently hidden after change of mind, remove them
    public List<Element<Respondent>> removeHiddenFields(List<Element<Respondent>> respondents) {
        respondents.forEach(respondentElement -> {
            Respondent respondent = respondentElement.getValue();

            if (NO.getValue().equals(respondent.getLegalRepresentation())) {
                respondent.setSolicitor(null);
            } else if (YES.getValue().equals(respondent.getLegalRepresentation())) {
                if (isNotEmpty(respondent.getSolicitor().getOrganisation())
                    && isNotEmpty(respondent.getSolicitor().getOrganisation().getOrganisationID())) {
                    respondent.getSolicitor().setUnregisteredOrganisation(null);
                } else {
                    respondent.getSolicitor().setRegionalOfficeAddress(null);
                }
            }
        });

        return respondents;
    }

    public List<Respondent> getRespondentsWithLegalRepresentation(List<Element<Respondent>> respondents) {
        return respondents
            .stream()
            .map(Element::getValue)
            .filter(respondent -> !isNull(respondent.getLegalRepresentation()) && respondent.getLegalRepresentation()
                .equals(YES.getValue()))
            .collect(Collectors.toList());
    }

    public List<String> getRespondentSolicitorEmails(List<Respondent> respondents) {
        return respondents
            .stream()
            .map(Respondent::getSolicitor)
            .map(RespondentSolicitor::getEmail)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private String getRespondentFullName(RespondentParty respondentParty) {
        String firstName = defaultIfNull(respondentParty.getFirstName(), "");
        String lastName = defaultIfNull(respondentParty.getLastName(), "");

        return String.format("%s %s", firstName, lastName);
    }

    public List<RespondentSolicitor> getRegisteredSolicitors(List<Element<Respondent>> respondents) {
        return unwrapElements(respondents)
            .stream()
            .filter(respondent -> YES.getValue().equals(respondent.getLegalRepresentation())
                && respondent.hasRegisteredOrganisation())
            .map(Respondent::getSolicitor)
            .collect(Collectors.toList());
    }

    public List<RespondentSolicitor> getUnregisteredSolicitors(List<Element<Respondent>> respondents) {
        return unwrapElements(respondents)
            .stream()
            .filter(respondent -> YES.getValue().equals(respondent.getLegalRepresentation())
                && respondent.hasUnregisteredOrganisation())
            .map(Respondent::getSolicitor)
            .collect(Collectors.toList());
    }

    public List<ChangeOrganisationRequest> getRepresentationChanges(List<Element<Respondent>> after,
                                                                    List<Element<Respondent>> before) {

        final List<Element<Respondent>> newRespondents = defaultIfNull(after, new ArrayList<>());
        final List<Element<Respondent>> oldRespondents = defaultIfNull(before, new ArrayList<>());

        final Map<UUID, Organisation> newRespondentsOrganisations = organisationByRespondentId(newRespondents);
        final Map<UUID, Organisation> oldRespondentsOrganisations = organisationByRespondentId(oldRespondents);

        final List<ChangeOrganisationRequest> changeRequests = new ArrayList<>();

        for (int i = 0; i < newRespondents.size(); i++) {
            SolicitorRole solicitorRole = SolicitorRole.values(SolicitorRole.Representing.RESPONDENT).get(i);
            UUID respondentId = newRespondents.get(i).getId();

            Organisation newOrganisation = newRespondentsOrganisations.get(respondentId);
            Organisation oldOrganisation = oldRespondentsOrganisations.get(respondentId);

            if (!Objects.equals(newOrganisation, oldOrganisation)) {
                changeRequests.add(changeRequest(newOrganisation, oldOrganisation, solicitorRole));
            }
        }

        return changeRequests;
    }

    private Map<UUID, Organisation> organisationByRespondentId(List<Element<Respondent>> respondents) {
        return respondents.stream().collect(
            HashMap::new,
            (container, respondent) -> container.put(respondent.getId(), getOrganisation(respondent.getValue())),
            HashMap::putAll
        );
    }

    private ChangeOrganisationRequest changeRequest(Organisation newOrganisation,
                                                    Organisation oldOrganisation,
                                                    SolicitorRole solicitorRole) {

        final DynamicListElement roleItem = DynamicListElement.builder()
            .code(solicitorRole.getCaseRoleLabel())
            .label(solicitorRole.getCaseRoleLabel())
            .build();

        return ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .requestTimestamp(time.now())
            .caseRoleId(DynamicList.builder()
                .value(roleItem)
                .listItems(List.of(roleItem))
                .build())
            .organisationToRemove(oldOrganisation)
            .organisationToAdd(newOrganisation)
            .build();
    }

    private Organisation getOrganisation(Respondent respondent) {
        return Optional.ofNullable(respondent)
            .map(Respondent::getSolicitor)
            .map(RespondentSolicitor::getOrganisation)
            .filter(org -> isNotEmpty(org.getOrganisationID()))
            .orElse(null);
    }

}
