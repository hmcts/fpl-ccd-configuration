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
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.document.UploadDocumentsMigrationService;

import java.time.LocalDate;
import java.util.List;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {

    private static final String MIGRATION_ID_KEY = "migrationId";

    private final UploadDocumentsMigrationService uploadDocumentsMigrationService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Object migrationId = getMigrationId(caseDetails);
        String familyManCaseNumber = caseData.getFamilyManCaseNumber();

        if (isCorrectCase(migrationId, familyManCaseNumber)) {
            log.info("Removing hearings from case reference {}", caseDetails.getId());
            caseDetails.getData().put("hearingDetails", removeHearings(caseData.getHearingDetails()));
            caseDetails.getData().remove(MIGRATION_ID_KEY);
        }
        if (isDocumentMigrationRequired(migrationId)) {
            log.info("Migration of Documents to Application Documents");
            uploadDocumentsMigrationService.transformFromOldCaseData(caseData);
            caseDetails.getData().remove(MIGRATION_ID_KEY);
        }

        return respond(caseDetails);
    }

    private boolean isDocumentMigrationRequired(Object migrationId) {
        return "FPLA-2379".equals(migrationId);
    }

    private boolean isCorrectCase(Object migrationId, String familyManCaseNumber) {
        return "FPLA-2437".equals(migrationId) && "ZW20C50003".equals(familyManCaseNumber);
    }

    private Object getMigrationId(CaseDetails caseDetails) {
        return caseDetails.getData().get(MIGRATION_ID_KEY);
    }

    private List<Element<HearingBooking>> removeHearings(List<Element<HearingBooking>> hearings) {
        for (int i = 0; i < 3; i++) {
            assertHearingDate(hearings.get(i).getValue());
            log.info("hearing {} has correct date", i);
        }
        log.info("Removing hearing 3");
        hearings.remove(2);
        log.info("Removing hearing 1");
        hearings.remove(0);
        return hearings;
    }

    private void assertHearingDate(HearingBooking hearing) {
        if (!LocalDate.of(2020, 11, 10).isEqual(hearing.getStartDate().toLocalDate())) {
            throw new IllegalArgumentException(String.format("Invalid hearing date %s", hearing.getStartDate()));
        }
    }
}
