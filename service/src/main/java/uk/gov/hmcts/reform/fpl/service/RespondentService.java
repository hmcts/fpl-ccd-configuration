package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType.LIVE_IN_REFUGE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
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
    public List<Element<Respondent>> consolidateAndRemoveHiddenFields(List<Element<Respondent>> respondents) {
        return respondents.stream().map(respondentElement -> {
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

            RespondentParty party = respondent.getParty();
            if (party != null) {
                RespondentParty.RespondentPartyBuilder partyBuilder = party.toBuilder();

                // Make as confidential if living in a refuge
                if (LIVE_IN_REFUGE.getValue().equalsIgnoreCase(party.getAddressKnow())) {
                    partyBuilder = partyBuilder.contactDetailsHidden(YES.getValue());
                }

                // Clear address not know reason if address is known
                if (!NO.getValue().equals(party.getAddressKnow()) && isNotEmpty(party.getAddressNotKnowReason())) {
                    partyBuilder = partyBuilder.addressNotKnowReason(null);
                } else if (NO.getValue().equals(party.getAddressKnow())
                           && party.getAddress() != null && isNotEmpty(party.getAddress().getAddressLine1())) {
                    partyBuilder = partyBuilder.address(Address.builder().build());
                }

                return element(respondentElement.getId(), respondent.toBuilder().party(partyBuilder.build()).build());
            }
            return respondentElement;
        }).toList();
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

    public List<String> getRespondentSolicitorTelephones(List<Respondent> respondents) {
        return respondents
            .stream()
            .map(Respondent::getSolicitor)
            .map(RespondentSolicitor::getTelephoneNumber)
            .filter(Objects::nonNull)
            .map(Telephone::getTelephoneNumber)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    }

    private String getRespondentFullName(RespondentParty respondentParty) {
        String firstName = defaultIfNull(respondentParty.getFirstName(), "");
        String lastName = defaultIfNull(respondentParty.getLastName(), "");

        return String.format("%s %s", firstName, lastName);
    }

    public List<Respondent> getRespondentsWithRegisteredSolicitors(List<Element<Respondent>> respondents) {
        return unwrapElements(respondents)
            .stream()
            .filter(respondent -> YES.getValue().equals(respondent.getLegalRepresentation())
                && respondent.hasRegisteredOrganisation())
            .collect(Collectors.toList());
    }

    public List<Respondent> getRespondentsWithUnregisteredSolicitors(List<Element<Respondent>> respondents) {
        return unwrapElements(respondents)
            .stream()
            .filter(respondent -> YES.getValue().equals(respondent.getLegalRepresentation())
                && respondent.hasUnregisteredOrganisation())
            .collect(Collectors.toList());
    }

    public boolean hasAddressChange(List<Element<Respondent>> after, List<Element<Respondent>> before) {
        return PeopleInCaseHelper.hasAddressChange(Collections.unmodifiableList(after),
            Collections.unmodifiableList(before));
    }

    public List<ChangeOrganisationRequest> getRepresentationChanges(List<Element<WithSolicitor>> after,
                                                                    List<Element<WithSolicitor>> before,
                                                                    SolicitorRole.Representing target) {

        final List<Element<WithSolicitor>> newRespondents = defaultIfNull(after, new ArrayList<>());
        final List<Element<WithSolicitor>> oldRespondents = defaultIfNull(before, new ArrayList<>());

        final Map<UUID, Organisation> newRespondentsOrganisations = organisationByRespondentId(newRespondents);
        final Map<UUID, Organisation> oldRespondentsOrganisations = organisationByRespondentId(oldRespondents);

        final List<ChangeOrganisationRequest> changeRequests = new ArrayList<>();

        for (int i = 0; i < newRespondents.size(); i++) {
            SolicitorRole solicitorRole = SolicitorRole.values(target).get(i);
            UUID respondentId = newRespondents.get(i).getId();

            Organisation newOrganisation = newRespondentsOrganisations.get(respondentId);
            Organisation oldOrganisation = oldRespondentsOrganisations.get(respondentId);

            if (!Objects.equals(newOrganisation, oldOrganisation)) {
                changeRequests.add(changeRequest(newOrganisation, oldOrganisation, solicitorRole));
            }
        }

        return changeRequests;
    }

    private Map<UUID, Organisation> organisationByRespondentId(List<Element<WithSolicitor>> respondents) {
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

    private Organisation getOrganisation(WithSolicitor respondent) {
        return Optional.ofNullable(respondent)
            .map(WithSolicitor::getSolicitor)
            .map(RespondentSolicitor::getOrganisation)
            .filter(org -> isNotEmpty(org.getOrganisationID()))
            .orElse(null);
    }

    public List<Element<Respondent>> getSelectedRespondents(CaseData caseData, String allRespondentsSelected) {
        return getSelectedRespondents(caseData.getAllRespondents(), caseData.getRespondentsSelector(),
            allRespondentsSelected);
    }

    public List<Element<Respondent>> getSelectedRespondents(List<Element<Respondent>> respondents, Selector selector,
                                                  String allRespondentsSelected) {

        if (useAllRespondents(allRespondentsSelected)) {
            return respondents;
        } else {
            if (isNull(selector) || isEmpty(selector.getSelected())) {
                return Collections.emptyList();
            }
            return selector.getSelected().stream()
                .map(respondents::get)
                .collect(toList());
        }
    }

    private boolean useAllRespondents(String sendPlacementNoticeToAllRespondents) {
        return "Yes".equals(sendPlacementNoticeToAllRespondents);
    }

    public Respondent transformOtherToRespondent(Other other) {
        RespondentParty respondentParty = RespondentParty.builder()
            .address(other.getAddress())
            .addressKnow(other.getAddressKnow())
            .addressNotKnowReason(other.getAddressNotKnowReason())
            .contactDetailsHidden(other.getDetailsHidden())
            .contactDetailsHiddenReason(other.getDetailsHiddenReason())
            .dateOfBirth(other.toParty().getDateOfBirth())
            .email(other.toParty().getEmail())
            .firstName(other.getName()) // other does not have first name, use other.getName() instead
            .gender(other.getGender())
            .genderIdentification(other.getGenderIdentification())
            //.lastName() // other does not have last name
            .litigationIssuesDetails(other.getLitigationIssuesDetails())
            .litigationIssues(other.getLitigationIssues())
            .organisationName(other.toParty().getOrganisationName())
            .partyType(other.toParty().getPartyType())
            .placeOfBirth(other.getBirthPlace())
            .relationshipToChild(other.getChildInformation())
            .telephoneNumber(other.toParty().getTelephoneNumber())
            .build();

        return Respondent.builder()
            .representedBy(other.getRepresentedBy())
            .party(respondentParty).build();
    }

}
