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

import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {

    private final UploadDocumentsMigrationService uploadDocumentsMigrationService;

    private static final String MIGRATION_ID_KEY = "migrationId";

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2379".equals(migrationId)) {
            run2379(caseDetails);
        }
        if ("FPLA-2525".equals(migrationId)) {
            run2525(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2525(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("SN20C50028".equals(caseData.getFamilyManCaseNumber())) {
            List<Element<HearingBooking>> hearings = caseData.getHearingDetails();

            if (hearings.isEmpty()) {
                throw new IllegalArgumentException("No hearings in a case");
            }

            Element<HearingBooking> hearingToBeRemoved = hearings.get(0);

            if (hearingToBeRemoved.getValue().getStartDate() != null) {
                throw new IllegalArgumentException(
                    format("Invalid hearing date %s", hearingToBeRemoved.getValue().getStartDate().toLocalDate()));
            }
            if (hearingToBeRemoved.getValue().getEndDate() != null) {
                throw new IllegalArgumentException(
                    format("Invalid hearing end date %s", hearingToBeRemoved.getValue().getEndDate().toLocalDate()));
            }
            if (hearingToBeRemoved.getValue().getType() != null) {
                throw new IllegalArgumentException(
                    format("Invalid hearing type %s", hearingToBeRemoved.getValue().getType()));
            }

            if (hearingToBeRemoved.getValue().getVenue() != null) {
                throw new IllegalArgumentException(
                    format("Invalid hearing venue %s", hearingToBeRemoved.getValue().getVenue()));
            }

            hearings.remove(hearingToBeRemoved);

            caseDetails.getData().put("hearingDetails", hearings);
        }
    }

    private void run2379(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        log.info("Migration of Old Documents to Application Documents for case ID {}",
            caseData.getId());
        Map<String, Object> data = caseDetails.getData();
        data.putAll(uploadDocumentsMigrationService.transformFromOldCaseData(caseData));
    }

}
