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
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
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

        if ("ZW20C50003".equals(caseData.getFamilyManCaseNumber())) {
            log.info("Removing hearings from case reference {}", caseDetails.getId());
            caseDetails.getData().put("hearingDetails", removeHearings(caseData.getHearingDetails()));
        }

        return respond(caseDetails);
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

    private List<Element<HearingBooking>> removeHearings(List<Element<HearingBooking>> hearings) {
        for (int i = 0; i < 3; i++) {
            assertHearingDate(hearings.get(i).getValue());
            log.info("hearing {} has correct date", i);
        }
        hearings.remove(2);
        hearings.remove(0);
        return hearings;
    }

    private void assertHearingDate(HearingBooking hearing) {
        if (!LocalDate.of(2020, 11, 10).isEqual(hearing.getStartDate().toLocalDate())) {
            throw new IllegalArgumentException(String.format("Invalid hearing date %s", hearing.getStartDate()));
        }
    }
}
