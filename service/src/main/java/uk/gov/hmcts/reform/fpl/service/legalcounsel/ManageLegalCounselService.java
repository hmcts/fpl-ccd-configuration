package uk.gov.hmcts.reform.fpl.service.legalcounsel;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
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
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.CaseRoleLookupService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.CHILD;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeCollection;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@RequiredArgsConstructor
@Component
public class ManageLegalCounselService {

    private static final List<SolicitorRole.Representing> RELEVANT_REPRESENTED_PARTY_TYPES = asList(RESPONDENT, CHILD);

    private static final String UNREGISTERED_USER_ERROR_MESSAGE_TEMPLATE = "Unable to grant access "
        + "[%s is not a Registered User] - "
        + "Email address for Counsel/External solicitor is not registered on the system. "
        + "They can register at https://manage-org.platform.hmcts.net/register-org/register";

    // Barrister-specific error messages
    private static final String ERROR_MESSAGE_CANNOT_ADD_NEW = "Unable to add new legal counsellor. "
        + "If you wish to add a new legal counsellor, please contact the the corresponding solicitors.";
    private static final String ERROR_MESSAGE_NOT_CURRENT_USER = "The email address cannot be changed. "
        + "If you wish to remove yourself from the case, please delete your entry.";

    private final CaseConverter caseConverter;
    private final CaseRoleLookupService caseRoleLookupService;
    private final OrganisationService organisationService;
    private final CaseAccessService caseAccessService;
    private final UserService userService;

    public List<Element<LegalCounsellor>> retrieveLegalCounselForLoggedInSolicitor(CaseData caseData) {
        Long caseId = caseData.getId();
        Set<CaseRole> caseRoles = caseAccessService.getUserCaseRoles(caseId);
        // if we are adding more condition in the future,
        // we can use strategy design pattern instead of having the same if-else in every method
        if (isBarrister(caseRoles)) {
            return retrieveLegalCounselForBarrister(caseData);
        } else {
            return retrieveLegalCounselForSolicitorRoles(caseData, caseRoles);
        }
    }

    private List<Element<LegalCounsellor>> retrieveLegalCounselForSolicitorRoles(CaseData caseData,
                                                                                 Set<CaseRole> caseRoles) {
        List<SolicitorRole> caseSolicitorRoles = caseRoleLookupService.getCaseSolicitorRolesByCaseRoles(caseRoles);
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

    private  List<Element<LegalCounsellor>> retrieveLegalCounselForBarrister(CaseData caseData) {
        String currentUserEmail = userService.getUserEmail();

        // Retrieves a list of legal counsellors associated with the currently logged-in barrister
        // by filtering respondents and children whose legal counsellor email matches the user's email.
        return Stream.concat(
                caseData.getRespondents1().stream(),
                caseData.getChildren1().stream())
            .map(Element::getValue)
            .map(WithSolicitor::getLegalCounsellors)
            .filter(ObjectUtils::isNotEmpty)
            .flatMap(List::stream)
            .filter(leagalCounsellorElement ->
                Objects.equals(leagalCounsellorElement.getValue().getEmail(), currentUserEmail))
            .findFirst().stream()
            .toList();
    }

    public void updateLegalCounsel(CaseDetails caseDetails) {
        CaseData caseData = caseConverter.convert(caseDetails);
        Long caseId = caseData.getId();

        List<Element<LegalCounsellor>> legalCounsellors = populateWithUserIds(
            caseData.getManageLegalCounselEventData().getLegalCounsellors()
        );

        Set<CaseRole> caseRoles = caseAccessService.getUserCaseRoles(caseId);
        if (isBarrister(caseRoles)) {
            updateLegalCounselForBarrister(caseDetails, caseData, legalCounsellors);
        } else {
            updateLegalCounselForSolicitorRoles(caseDetails, caseData, legalCounsellors, caseRoles);
        }

        caseDetails.getData().remove("legalCounsellors");
    }

    private void updateLegalCounselForSolicitorRoles(CaseDetails caseDetails, CaseData caseData,
                                                     List<Element<LegalCounsellor>> legalCounsellors,
                                                     Set<CaseRole> caseRoles) {
        List<SolicitorRole> caseSolicitorRoles = caseRoleLookupService.getCaseSolicitorRolesByCaseRoles(caseRoles);

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
    }

    private void updateLegalCounselForBarrister(CaseDetails caseDetails, CaseData caseData,
                                                List<Element<LegalCounsellor>> legalCounsellors) {
        String currentUserEmail = userService.getUserEmail();

        // update respondent legal counsellors
        List<Element<Respondent>> respondents = caseData.getRespondents1();
        respondents.stream()
            .map(Element::getValue)
            .forEach(respondent ->
                updateAndSetLegalCounsellorsForBarrister(respondent, legalCounsellors, currentUserEmail));
        caseDetails.getData().put("respondents1", respondents);

        // update children legal counsellors
        List<Element<Child>> children = caseData.getChildren1();
        children.stream()
            .map(Element::getValue)
            .forEach(child ->
                updateAndSetLegalCounsellorsForBarrister(child, legalCounsellors, currentUserEmail));
        caseDetails.getData().put("children1", children);
    }

    private void updateAndSetLegalCounsellorsForBarrister(WithSolicitor withSolicitor,
                                                          List<Element<LegalCounsellor>> legalCounsellors,
                                                          String currentUserEmail) {
        if (withSolicitor.getLegalCounsellors() != null) {
            List<Element<LegalCounsellor>> updatedLegalCounsellors = withSolicitor.getLegalCounsellors().stream()
                .map(legalCounsellorElement ->
                    // update the existing elements if exists in the updated list
                    findElement(legalCounsellorElement.getId(), legalCounsellors)
                        // remove the element if not exists in the updated list and the email is same as current user
                        .orElse(Objects.equals(legalCounsellorElement.getValue().getEmail(), currentUserEmail)
                            ? null : legalCounsellorElement))
                .filter(Objects::nonNull)
                .toList();
            withSolicitor.setLegalCounsellors(updatedLegalCounsellors.isEmpty()
                ? null : updatedLegalCounsellors);
        }
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

        if (isBarrister(caseAccessService.getUserCaseRoles(caseData.getId()))) {
            errorMessages.addAll(validateEventDataForBarrister(caseData));
        }

        return errorMessages;
    }

    private List<String> validateEventDataForBarrister(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        List<Element<LegalCounsellor>> updatedLegalCounsels =
            caseData.getManageLegalCounselEventData().getLegalCounsellors();

        if (updatedLegalCounsels.size() > 1) {
            errors.add(ERROR_MESSAGE_CANNOT_ADD_NEW);
        } else if (!updatedLegalCounsels.isEmpty()) {
            String currentUserEmail = userService.getUserEmail();
            boolean hasEmailChanged = updatedLegalCounsels.stream()
                .map(Element::getValue)
                .map(LegalCounsellor::getEmail)
                .anyMatch(email -> !Objects.equals(email, currentUserEmail));
            if (hasEmailChanged) {
                errors.add(ERROR_MESSAGE_NOT_CURRENT_USER);
            }
        }
        return errors;
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

    private boolean isBarrister(Set<CaseRole> caseRoles) {
        return nullSafeCollection(caseRoles).stream().anyMatch(CaseRole.BARRISTER::equals);
    }
}
