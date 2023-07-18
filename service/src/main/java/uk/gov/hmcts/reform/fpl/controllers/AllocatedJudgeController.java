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
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/allocated-judge")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllocatedJudgeController extends CallbackController {
    private final ValidateEmailService validateEmailService;
    private final JudicialService judicialService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().put("enterManually", "No"); // default choice to No
        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        Optional<String> error;

        if (caseData.getEnterManually().equals(YesNo.NO)) {
            // validate judge
            error = judicialService.validateJudicialUserField(caseData);
        } else {
            // validate manual judge details
            String email = caseData.getAllocatedJudge().getJudgeEmailAddress();
            error = validateEmailService.validate(email);
        }

        if (error.isPresent()) {
            return respond(caseDetails, List.of(error.get()));
        }

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        // Fill in judge details
        if (caseData.getEnterManually().equals(YesNo.NO)
            && !isEmpty(caseData.getJudicialUser())
            && !isEmpty(caseData.getJudicialUser().getPersonalCode())) {

            Optional<JudicialUserProfile> jup = judicialService.getJudge(caseData.getJudicialUser().getPersonalCode());
            if (jup.isPresent()) {
                caseDetails.getData().put("allocatedJudge", Judge.fromJudicialUserProfile(jup.get()));
                // todo - move this to submitted callback
                // judicialService.assignAllocatedJudge(caseDetails.getId(), jup.get().getSidamId());
            } else {
                return respond(caseDetails,
                    List.of("Could not fetch Judge details from JRD, please try again in a few minutes."));
            }
        }


        removeTemporaryFields(caseDetails, "judicialUser", "enterManually");
        return respond(caseDetails);
    }
}
