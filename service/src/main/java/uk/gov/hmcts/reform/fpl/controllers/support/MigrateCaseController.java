package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
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
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.NoticeOfChangeAnswersData;
import uk.gov.hmcts.reform.fpl.model.RespondentPolicyData;
import uk.gov.hmcts.reform.fpl.service.RespondentRepresentationService;

import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private final RespondentRepresentationService respondentRepresentationService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2961".equals(migrationId)) {
            run2961(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2961(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (isUnsupportedState(caseData.getState()) || containsNoCFields(caseData)) {
            throw new IllegalStateException(String.format("Migration failed on case %s: Unexpected migration",
                caseDetails.getId()));
        }

        if (caseData.getRespondents1().size() > 10) {
            throw new IllegalStateException(String.format("Migration failed on case %s: Case has %s respondents",
                caseDetails.getId(), caseData.getRespondents1().size()));
        }

        Map<String, Object> data = caseDetails.getData();

        data.putAll(respondentRepresentationService.generateForSubmission(caseData));
    }

    private boolean isUnsupportedState(State state) {
        List<State> unsupportedStates = List.of(
            State.OPEN,
            State.CLOSED,
            State.DELETED
        );

        return unsupportedStates.contains(state);
    }

    private boolean containsNoCFields(CaseData caseData) {
        RespondentPolicyData emptyRespondentPolicyData = RespondentPolicyData.builder().build();
        NoticeOfChangeAnswersData emptyNoCAnswerData = NoticeOfChangeAnswersData.builder().build();

        return !emptyNoCAnswerData.equals(caseData.getNoticeOfChangeAnswersData())
            || !emptyRespondentPolicyData.equals(caseData.getRespondentPolicyData());
    }

}
