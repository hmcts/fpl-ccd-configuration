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
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.judicial.NewAllocatedJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Slf4j
@RestController
@RequestMapping("/callback/allocated-judge")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllocatedJudgeController extends CallbackController {
    private final ValidateEmailService validateEmailService;
    private final JudicialService judicialService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().put("enterManually", NO.getValue());
        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        Optional<String> error = judicialService.validateAllocatedJudge(caseData);

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
        if (caseData.getEnterManually().equals(YesNo.NO)) {

            if (isEmpty(caseData.getJudicialUser()) || isEmpty(caseData.getJudicialUser().getPersonalCode())) {
                return respond(caseDetails,
                    List.of("You must search for a judge or enter their details manually"));
            }

            Optional<JudicialUserProfile> jup = judicialService.getJudge(caseData.getJudicialUser().getPersonalCode());
            if (jup.isPresent()) {
                caseDetails.getData().put("allocatedJudge", Judge.fromJudicialUserProfile(jup.get()));
            } else {
                return respond(caseDetails,
                    List.of("Could not fetch Judge details from JRD, please try again in a few minutes."));
            }
        } else {
            // entering manually, check against our lookup tables, they may be a legal adviser
            Optional<String> possibleId = judicialService
                .getJudgeUserIdFromEmail(caseData.getAllocatedJudge().getJudgeEmailAddress());

            // if they are in our maps - add their UUID extra info to the case
            possibleId.ifPresentOrElse(s -> caseDetails.getData().put("allocatedJudge",
                caseData.getAllocatedJudge().toBuilder()
                    .judgeJudicialUser(JudicialUser.builder()
                        .idamId(s)
                        .build())
                    .build()),
                () -> {
                    Judge allocatedJudge = caseData.getAllocatedJudge();
                    if (JudgeOrMagistrateTitle.MAGISTRATES.equals(allocatedJudge.getJudgeTitle())) {
                        allocatedJudge = allocatedJudge.toBuilder().judgeLastName(null).build();
                    } else {
                        allocatedJudge = allocatedJudge.toBuilder().judgeFullName(null).build();
                    }

                    caseDetails.getData().put("allocatedJudge", allocatedJudge);
                });
        }

        removeTemporaryFields(caseDetails, "judicialUser", "enterManually");
        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        publishEvent(new NewAllocatedJudgeEvent(caseData.getAllocatedJudge(), caseData.getId()));
        publishEvent(
            new AfterSubmissionCaseDataUpdated(getCaseData(callbackRequest), getCaseDataBefore(callbackRequest))
        );

    }
}
