package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

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
        CaseData caseData = getCaseData(caseDetails);
        Object migrationId = getMigrationId(caseDetails);
        String familyManCaseNumber = caseData.getFamilyManCaseNumber();

        if (isCorrectCase(migrationId, familyManCaseNumber, "FPLA-2429", "CF20C50024")) {
            log.info("Removing others from case reference {}", caseDetails.getId());
            Others others = caseData.getOthers();
            caseDetails.getData().put("others", nullFirstOther(others));
            caseDetails.getData().remove(MIGRATION_ID_KEY);
        }

        if (isCorrectCase(migrationId, familyManCaseNumber, "FPLA-2450", "CF20C50014")) {
            log.info("Removing c2 document bundle from case reference {}", caseDetails.getId());
            caseDetails.getData().put("c2DocumentBundle", removeC2Document(caseData.getC2DocumentBundle()));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private boolean isCorrectCase(Object migrationId, String familyManCaseNumber, String expectedMigrationId,
                                  String expectedFamilyManCaseNumber) {
        return expectedMigrationId.equals(migrationId) && expectedFamilyManCaseNumber.equals(familyManCaseNumber);
    }

    private List<Element<C2DocumentBundle>> removeC2Document(List<Element<C2DocumentBundle>> documentBundle) {
        // remove latest bundle (will be the last one added)
        documentBundle.remove(documentBundle.size() - 1);
        return documentBundle;
    }

    private Object getMigrationId(CaseDetails caseDetails) {
        return caseDetails.getData().get(MIGRATION_ID_KEY);
    }

    private Others nullFirstOther(Others others) {
        return others.toBuilder().firstOther(null).build();
    }
}
