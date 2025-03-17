package uk.gov.hmcts.reform.fpl.service.legalcounsel;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorAdded;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorEvent;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageLegalCounselEventData;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.CaseRoleLookupService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.rd.model.Organisation;

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
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@RequiredArgsConstructor
@Component
public class ManageLegalCounselService {

    private static final List<SolicitorRole.Representing> RELEVANT_REPRESENTED_PARTY_TYPES = asList(RESPONDENT, CHILD);

    private static final String UNREGISTERED_USER_ERROR_MESSAGE_TEMPLATE = "Unable to grant access "
        + "[%s is not a Registered User] - "
        + "Email address for Counsel/External solicitor is not registered on the system. "
        + "They can register at https://manage-org.platform.hmcts.net/register-org/register";

    private final CaseConverter caseConverter;
    private final CaseRoleLookupService caseRoleLookupService;
    private final OrganisationService organisationService;

    public List<Element<LegalCounsellor>> retrieveLegalCounselForLoggedInSolicitor(CaseData caseData) {
        Long caseId = caseData.getId();
        List<SolicitorRole> caseSolicitorRoles = caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(caseId);
        return retrieveLegalCounselForRoles(caseData, caseSolicitorRoles);
    }

    public List<Element<LegalCounsellor>> retrieveLegalCounselForRoles(CaseData caseData,
                                                                       List<SolicitorRole> caseSolicitorRoles) {
        return caseSolicitorRoles.stream()
            .filter(solicitorRole -> RELEVANT_REPRESENTED_PARTY_TYPES.contains(solicitorRole.getRepresenting()))
            .findFirst()
            .map(solicitorRole -> solicitorRole.getRepresenting()
                .getTarget()
                .apply(caseData)
                .get(solicitorRole.getIndex())
            )
            .map(Element::getValue)
            .map(WithSolicitor::getLegalCounsellors)
            .orElse(emptyList());
    }

    public void updateLegalCounsel(CaseDetails caseDetails) {
        CaseData caseData = caseConverter.convert(caseDetails);
        Long caseId = caseData.getId();
        List<SolicitorRole> caseSolicitorRoles = caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(caseId);

        List<Element<LegalCounsellor>> legalCounsellors = populateWithUserIds(
            caseData.getManageLegalCounselEventData().getLegalCounsellors()
        );

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
        List<LegalCounsellor> legalCounsellors = unwrapElements(manageLegalCounselEventData.getLegalCounsellors());

        legalCounsellors.stream()
            .filter(legalCounsellor -> organisationService.findUserByEmail(legalCounsellor.getEmail()).isEmpty())
            .map(legalCounsellor -> format(UNREGISTERED_USER_ERROR_MESSAGE_TEMPLATE, legalCounsellor.getFullName()))
            .forEach(errorMessages::add);

        legalCounsellors.stream()
            .filter(legalCounsellor -> Optional.ofNullable(legalCounsellor.getOrganisation())
                .map(uk.gov.hmcts.reform.ccd.model.Organisation::getOrganisationID)
                .filter(not(Strings::isNullOrEmpty))
                .isEmpty())
            .map(legalCounsellor -> format(
                "Legal counsellor %s %s has no selected organisation",
                legalCounsellor.getFirstName(), legalCounsellor.getLastName()
            ))
            .forEach(errorMessages::add);

        return errorMessages;
    }

    public List<LegalCounsellorEvent> runFinalEventActions(CaseData previousCaseData, CaseData currentCaseData) {
        List<LegalCounsellorEvent> events = new ArrayList<>();

        String orgName = organisationService.findOrganisation()
            .map(Organisation::getName)
            .orElseThrow();

        List<LegalCounsellor> currentLegalCounsellors = unwrapElements(
            retrieveLegalCounselForLoggedInSolicitor(currentCaseData)
        );
        List<LegalCounsellor> previousLegalCounsellors = unwrapElements(
            retrieveLegalCounselForLoggedInSolicitor(previousCaseData)
        );

        //Revoke case access from all removed legal counsellors
        previousLegalCounsellors.stream()
            .filter(not(currentLegalCounsellors::contains))
            .forEach(counsellor -> events.add(new LegalCounsellorRemoved(currentCaseData, orgName, counsellor)));

        //Grant case access to all new legal counsellors
        currentLegalCounsellors.stream()
            .filter(not(previousLegalCounsellors::contains))
            .forEach(counsellor -> events.add(new LegalCounsellorAdded(currentCaseData, counsellor)));

        return events;
    }

    private List<Element<LegalCounsellor>> populateWithUserIds(List<Element<LegalCounsellor>> counsellors) {
        return counsellors.stream().map(counsellorElement -> {
            LegalCounsellor counsellor = counsellorElement.getValue();
            String userId = organisationService.findUserByEmail(counsellor.getEmail()).orElseThrow();
            return element(counsellorElement.getId(), counsellor.toBuilder().userId(userId).build());
        }).toList();
    }
}
