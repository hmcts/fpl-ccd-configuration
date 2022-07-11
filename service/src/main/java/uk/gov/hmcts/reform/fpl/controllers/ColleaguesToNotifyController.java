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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@Api
@RestController
@RequestMapping("/callback/add-colleagues-to-notify")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ColleaguesToNotifyController extends CallbackController {

    private final UserService userService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        final CaseDetails caseDetails = callbackrequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        Optional<WithSolicitor> represented = userService.caseRoleToRepresented(caseData);

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

        Optional<WithSolicitor> represented = userService.caseRoleToRepresented(caseData);

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
