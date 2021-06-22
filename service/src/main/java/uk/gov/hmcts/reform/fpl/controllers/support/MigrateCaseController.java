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
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;

import java.util.Objects;

import static uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData.reviewDecisionFields;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {

    private static final String MIGRATION_ID_KEY = "migrationId";

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-3170".equals(migrationId)) {
            run3170(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3170(CaseDetails caseDetails) {
        final String familyManCaseNumber = "WR21C50006";

        if (!Objects.equals(familyManCaseNumber, caseDetails.getData().get("familyManCaseNumber"))) {
            throw new AssertionError(String.format(
                "Migration FPLA-3170: Expected family man case number to be %s but was %s",
                familyManCaseNumber, caseDetails.getData().get("familyManCaseNumber")));
        }

        CaseDetailsHelper.removeTemporaryFields(caseDetails, reviewDecisionFields());
    }

}
