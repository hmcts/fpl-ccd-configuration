package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.service.CaseRoleLookupService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@RestController
@RequestMapping("/callback/add-colleagues-to-notify")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ColleaguesToNotifyController extends CallbackController {

    private final CaseRoleLookupService caseRoleLookupService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        final CaseDetails caseDetails = callbackrequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        List<SolicitorRole> roles = caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(caseData.getId());
        List<WithSolicitor> represented = roles.stream()
            .map(role -> role.getRepresentedPerson(caseData))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Element::getValue)
            .collect(Collectors.toList());

        if (represented.isEmpty()) {
            List<String> errors = List.of("There is no one this user is representing on this case.");
            return respond(caseDetails, errors);
        }

        // Use the first party we are representing - solicitors shouldn't be representing multiple
        // except for ChildSolicitors - we use the first anyway and copy it across all of the Children later
        caseDetails.getData().put("respondentName", represented.get(0).toParty().getFullName());
        caseDetails.getData().put("colleaguesToNotify",
            represented.get(0).getSolicitor().getColleaguesToBeNotified());

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        final CaseData caseData = getCaseData(callbackrequest);
        final CaseDetailsMap caseDetails = caseDetailsMap(callbackrequest.getCaseDetails());

        List<SolicitorRole> roles = caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(caseData.getId());
        List<WithSolicitor> represented = roles.stream()
            .map(role -> role.getRepresentedPerson(caseData))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Element::getValue)
            .collect(Collectors.toList());

        if (represented.isEmpty()) {
            List<String> errors = List.of("There is no one this user is representing on this case.");
            return respond(caseDetails, errors);
        }

        SolicitorRole.Representing representationType = roles.get(0).getRepresenting();

        if (SolicitorRole.Representing.RESPONDENT.equals(representationType)) {
            // Update the respondent who's solicitor it is
            List<Element<Respondent>> respondents = caseData.getRespondents1();

            for (Element<Respondent> respondent : respondents) {
                if (respondent.getValue().equals(represented.get(0))) {
                    respondent.getValue().getSolicitor().setColleaguesToBeNotified(caseData.getColleaguesToNotify());
                }
            }

            caseDetails.put("respondents1", respondents);
        } else if (SolicitorRole.Representing.CHILD.equals(representationType)) {
            List<Element<Child>> children = caseData.getChildren1();

            for (Element<Child> child : children) {
                // Update all children we represent
                if (represented.contains(child.getValue())) {
                    child.getValue().getSolicitor().setColleaguesToBeNotified(caseData.getColleaguesToNotify());
                }
            }

            caseDetails.put("children1", children);
        }

        // Remove temporary fields as not needed anymore
        caseDetails.removeAll("respondentName", "colleaguesToNotify");

        return respond(caseDetails);
    }
}
