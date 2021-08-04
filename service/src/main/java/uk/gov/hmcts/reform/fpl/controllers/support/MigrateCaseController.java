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
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.enums.State.DELETED;

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
        String migrationId = (String) caseDetails.getData().get(MIGRATION_ID_KEY);

        log.info("Migration " + migrationId);

        switch (migrationId) {
            case "FPLA-3294":
                run3294(caseDetails);

                caseDetails.getData().remove(MIGRATION_ID_KEY);
                AboutToStartOrSubmitCallbackResponse response = respond(caseDetails);
                response.setState(DELETED.getValue());
                return response;
            default:
                log.error("Unhandled migration {}", migrationId);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3294(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        validateFamilyManNumber("FPLA-3294", List.of("SA21C50089", "SA21C50091"), caseData);

        caseDetails.getData().clear();
        caseDetails.setState(DELETED.getValue());
    }

    private void validateFamilyManNumber(String migrationId, List<String> familyManCaseNumbers, CaseData caseData) {
        if (!familyManCaseNumbers.contains(caseData.getFamilyManCaseNumber())) {
            throw new AssertionError(format(
                "Migration %s: Family man number %s was not expected",
                migrationId, caseData.getFamilyManCaseNumber()));
        }
    }

}
