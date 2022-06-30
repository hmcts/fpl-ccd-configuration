package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@Api
@RestController
@RequestMapping("/callback/add-colleagues-to-notify")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ColleaguesToNotifyController extends CallbackController {

    private final UserService userService;

    private Optional<WithSolicitor> getRepresentedRespondent(Set<CaseRole> roles, CaseData caseData) {
        List<CaseRole> respondentSolicitorRoles = CaseRole.respondentSolicitors();
        // Check if they are a respondent solicitor
        for (int i = 0; i < respondentSolicitorRoles.size(); i++) {
            if (roles.contains(respondentSolicitorRoles.get(i))) {
                if (i > caseData.getRespondents1().size()) {
                    // this respondent doesn't exist so cannot have a solicitor
                    return Optional.empty();
                }
                return Optional.ofNullable(caseData.getRespondents1().get(i).getValue());
            }
        }
        return Optional.empty();
    }
    private Optional<WithSolicitor> getRepresentedChild(Set<CaseRole> roles, CaseData caseData) {
        List<CaseRole> childSolicitorRoles = CaseRole.childSolicitors();
        // Check if they are a respondent solicitor
        for (int i = 0; i < childSolicitorRoles.size(); i++) {
            if (roles.contains(childSolicitorRoles.get(i))) {
                if (i > caseData.getChildren1().size()) {
                    // this child doesn't exist so cannot have a solicitor
                    return Optional.empty();
                }
                return Optional.ofNullable(caseData.getChildren1().get(i).getValue());
            }
        }
        return Optional.empty();
    }

    private Optional<WithSolicitor> caseRoleToRepresented(CaseData caseData) {
        if (userService.isRespondentSolicitor(caseData.getId())) {
            return getRepresentedRespondent(userService.getCaseRoles(caseData.getId()), caseData);
        } else if (userService.isChildSolicitor(caseData.getId())) {
            return getRepresentedChild(userService.getCaseRoles(caseData.getId()), caseData);
        } else {
            return Optional.empty();
        }
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        final CaseDetails caseDetails = callbackrequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        Optional<WithSolicitor> represented = caseRoleToRepresented(caseData);

        List<String> errors;
        if (represented.isEmpty()) {
            errors = List.of("There is no one this user is representing on this case.");
            return respond(caseDetails, errors);
        }

        caseDetails.getData().put("respondentName", represented.get().toParty().getFullName());
        caseDetails.getData().put("colleaguesToNotify",
            represented.get().getSolicitor().getColleaguesToBeNotified());

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        final CaseData caseData = getCaseData(callbackrequest);
        final CaseDetailsMap caseDetails = caseDetailsMap(callbackrequest.getCaseDetails());

        Optional<WithSolicitor> represented = caseRoleToRepresented(caseData);

        List<String> errors;
        if (represented.isEmpty()) {
            errors = List.of("There is no one this user is representing on this case.");
            return respond(caseDetails, errors);
        }

        if (userService.isRespondentSolicitor(caseData.getId())) {
            // Update the respondent who's solicitor it is
            List<Element<Respondent>> respondents = caseData.getRespondents1();

            for (Element<Respondent> respondent : respondents) {
                if (respondent.getValue().equals(represented.get())) {
                    respondent.getValue().getSolicitor().setColleaguesToBeNotified(caseData.getColleaguesToNotify());
                }
            }

            caseDetails.put("respondents1", respondents);
        } else if (userService.isChildSolicitor(caseData.getId())) {
            // Update the child who's solicitor it is
            List<Element<Child>> children = caseData.getChildren1();

            for (Element<Child> child : children) {
                if (child.getValue().equals(represented.get())) {
                    child.getValue().getSolicitor().setColleaguesToBeNotified(caseData.getColleaguesToNotify());
                }
            }

            caseDetails.put("children1", children);
        }

        // Remove temporary fields as not needed anymore
        caseDetails.removeAll("respondentName", "colleaguesToNotify");

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest);

        // Send emails!
    }
}
