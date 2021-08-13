package uk.gov.hmcts.reform.fpl.service.legalcounsel;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.events.LegalCounsellorAdded;
import uk.gov.hmcts.reform.fpl.events.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageLegalCounselEventData;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.CaseRoleLookupService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.CHILD;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.RESPONDENT;

@RequiredArgsConstructor
@Component
public class ManageLegalCounselService {

    private static final List<SolicitorRole.Representing> RELEVANT_REPRESENTED_PARTY_TYPES = asList(RESPONDENT, CHILD);

    private static final String UNREGISTERED_USER_ERROR_MESSAGE_TEMPLATE = "Unable to grant access "
        + "[%s is not a Registered User] - Email address for Legal representative is not registered on the system. "
        + "They can register at https://manage-org.platform.hmcts.net/register-org/register";

    private final CaseConverter caseConverter;
    private final CaseRoleLookupService caseRoleLookupService;
    private final OrganisationService organisationService;

    public List<Element<LegalCounsellor>> retrieveLegalCounselForLoggedInSolicitor(CaseData caseData) {
        String caseId = caseData.getId().toString();
        List<SolicitorRole> caseSolicitorRoles = caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(caseId);

        return caseSolicitorRoles.stream()
            .filter(solicitorRole -> RELEVANT_REPRESENTED_PARTY_TYPES.contains(solicitorRole.getRepresenting()))
            .findFirst()
            .map(solicitorRole -> {
                int representedPartyIndex = solicitorRole.getIndex();
                return solicitorRole.getRepresenting().getTarget().apply(caseData).get(representedPartyIndex);
            })
            .map(Element::getValue)
            .map(WithSolicitor::getLegalCounsellors)
            .orElse(emptyList());
    }

    public void updateLegalCounsel(CaseDetails caseDetails) {
        CaseData caseData = caseConverter.convert(caseDetails);
        String caseId = caseData.getId().toString();
        List<SolicitorRole> caseSolicitorRoles = caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(caseId);

        List<Element<LegalCounsellor>> legalCounsellors =
            caseData.getManageLegalCounselEventData().getLegalCounsellors();

        Map<String, Object> data = caseDetails.getData();

        List<Element<Respondent>> allRespondents = caseData.getAllRespondents();
        caseSolicitorRoles.stream()
            .filter(solicitorRole -> solicitorRole.getRepresenting() == RESPONDENT)
            .map(SolicitorRole::getIndex)
            .map(allRespondents::get)
            .map(Element::getValue)
            .forEach(respondent -> respondent.setLegalCounsellors(legalCounsellors));
        data.put("respondents1", allRespondents);

        List<Element<Child>> allChildren = caseData.getAllChildren();
        caseSolicitorRoles.stream()
            .filter(solicitorRole -> solicitorRole.getRepresenting() == CHILD)
            .map(SolicitorRole::getIndex)
            .map(allChildren::get)
            .map(Element::getValue)
            .forEach(child -> child.setLegalCounsellors(legalCounsellors));
        data.put("children1", allChildren);

        data.remove("legalCounsellors");
    }

    public List<String> validateEventData(CaseData caseData) {
        List<String> errorMessages = new ArrayList<>();

        ManageLegalCounselEventData manageLegalCounselEventData = caseData.getManageLegalCounselEventData();
        List<Element<LegalCounsellor>> legalCounsellors = manageLegalCounselEventData.getLegalCounsellors();

        legalCounsellors.stream()
            .map(Element::getValue)
            .filter(legalCounsellor -> organisationService.findUserByEmail(legalCounsellor.getEmail()).isEmpty())
            .map(legalCounsellor -> format(UNREGISTERED_USER_ERROR_MESSAGE_TEMPLATE, legalCounsellor.getFullName()))
            .forEach(errorMessages::add);

        legalCounsellors.stream()
            .map(Element::getValue)
            .filter(legalCounsellor -> Optional.ofNullable(legalCounsellor.getOrganisation())
                .map(uk.gov.hmcts.reform.ccd.model.Organisation::getOrganisationID)
                .filter(not(Strings::isNullOrEmpty))
                .isEmpty())
            .map(legalCounsellor -> format("Legal counsellor %s %s has no selected organisation",
                legalCounsellor.getFirstName(),
                legalCounsellor.getLastName()))
            .forEach(errorMessages::add);

        return errorMessages;
    }

    public List<? super Object> runFinalEventActions(CaseData previousCaseData, CaseData currentCaseData) {
        List<? super Object> eventsToPublish = new ArrayList<>();

        String loggedInSolicitorOrganisationName = organisationService.findOrganisation()
            .map(organisation -> organisation.getName())
            .orElseThrow();

        List<LegalCounsellor> currentLegalCounsellors = retrieveLegalCounselForLoggedInSolicitor(currentCaseData)
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        List<LegalCounsellor> previousLegalCounsellors = retrieveLegalCounselForLoggedInSolicitor(previousCaseData)
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        //Grant access to all current legal counsellors
        List<LegalCounsellor> addedLegalCounsellors = ListUtils.getAddedItems(currentLegalCounsellors, previousLegalCounsellors)
            .collect(Collectors.toList());
        addedLegalCounsellors.stream()
            .map(legalCounsellor -> organisationService.findUserByEmail(legalCounsellor.getEmail())
                .map(userId -> Pair.of(userId, legalCounsellor)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(legalCounsellorPair -> eventsToPublish.add(
                new LegalCounsellorAdded(currentCaseData, legalCounsellorPair)
            ));

        //Revoke access from all that were in the previous case, but not in the new case
        List<LegalCounsellor> removedLegalCounsellors = ListUtils.getRemovedItems(currentLegalCounsellors, previousLegalCounsellors)
            .collect(Collectors.toList());
        removedLegalCounsellors.stream()
            .map(legalCounsellor -> organisationService.findUserByEmail(legalCounsellor.getEmail())
                .map(userId -> Pair.of(userId, legalCounsellor)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(legalCounsellorPair -> eventsToPublish.add(
                new LegalCounsellorRemoved(currentCaseData,
                    loggedInSolicitorOrganisationName,
                    legalCounsellorPair)
            ));

        return eventsToPublish;
    }

    public List<Element<Respondent>> updateLegalCounselForRemovedSolicitors(//TODO - I think I need to feature-toggle this
        List<Element<Respondent>> respondentsInPreviousCaseData,
        List<Element<Respondent>> respondentsInCurrentCaseData) {

        for (int i = 0; i < respondentsInPreviousCaseData.size(); i++) {
            final int index = i;
            Optional<String> organisationIdFromSolicitorInPreviousCaseData = Optional.of(respondentsInPreviousCaseData)
                .map(respondents -> respondents.get(index))
                .map(Element::getValue)
                .map(WithSolicitor::getSolicitor)
                .map(RespondentSolicitor::getOrganisation)
                .map(organisation -> organisation.getOrganisationID());

            Optional<WithSolicitor> respondentInCurrentCaseData = Optional.ofNullable(respondentsInCurrentCaseData)
                .map(respondents -> respondents.get(index))
                .map(Element::getValue);
            Optional<String> organisationIdFromSolicitorInCurrentCaseData = respondentInCurrentCaseData
                .map(WithSolicitor::getSolicitor)
                .map(RespondentSolicitor::getOrganisation)
                .map(organisation -> organisation.getOrganisationID());

            if (!organisationIdFromSolicitorInPreviousCaseData.equals(organisationIdFromSolicitorInCurrentCaseData)) {
                //TODO - add children later
                //Try getting legal counsel from any respondent within the same organisation
                List<Element<LegalCounsellor>> legalCounsel = organisationIdFromSolicitorInCurrentCaseData
                    .map(organisationIdForNewSolicitor -> getLegalCounselUsedByGivenOrganisation(organisationIdForNewSolicitor, respondentsInPreviousCaseData))
                    .orElse(emptyList());

                respondentInCurrentCaseData.ifPresent(respondent -> respondent.setLegalCounsellors(legalCounsel));
            }
        }

        return respondentsInCurrentCaseData;
    }

    private List<Element<LegalCounsellor>> getLegalCounselUsedByGivenOrganisation(String organisationId, List<Element<Respondent>> respondents) {
        return respondents.stream()
            .map(Element::getValue)
            .filter(respondent -> Optional.of(respondent)
                .map(Respondent::getSolicitor)
                .map(RespondentSolicitor::getOrganisation)
                .map(organisation -> organisation.getOrganisationID())
                .map(organisationId::equals)
                .orElse(false))
            .findFirst()
            .map(Respondent::getLegalCounsellors)
            .orElse(null);
    }

}
