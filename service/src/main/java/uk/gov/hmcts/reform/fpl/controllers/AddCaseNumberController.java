package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.CaseNumberAdded;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isAlphanumeric;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@RestController
@RequestMapping("/callback/add-case-number")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddCaseNumberController extends CallbackController {

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        return respond(caseDetails, validationErrors(caseDetails));
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData previousData = getCaseDataBefore(callbackRequest);

        if (isEmpty(previousData.getFamilyManCaseNumber())) {
            publishEvent(new CaseNumberAdded(getCaseData(callbackRequest)));
        } else {
            log.info("Robotics notification not sent on familyManCaseNumber update");
        }
    }

    private List<String> validationErrors(final CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (!isAlphanumeric(caseData.getFamilyManCaseNumber())) {
            return singletonList("Enter a valid FamilyMan case number");
        }

        return emptyList();
    }
}
