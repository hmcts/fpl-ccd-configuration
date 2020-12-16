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
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.document.UploadDocumentsMigrationService;

import java.time.LocalDate;
import java.time.Month;
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


        if ("FPLA-2491".equals(migrationId)) {
            run2491(caseDetails);
        }

        if ("FPLA-2379".equals(migrationId)) {
            run2379(caseDetails);
        }

        if ("FPLA-2501".equals(migrationId)) {
            run2501(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2491(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("SN20C50018".equals(caseData.getFamilyManCaseNumber())) {
            List<Element<HearingBooking>> hearings = caseData.getHearingDetails();

            LocalDate expectedHearing1Date = LocalDate.of(2021, Month.FEBRUARY, 5);
            LocalDate expectedHearing2Date = LocalDate.of(2021, Month.MARCH, 4);

            if (hearings.size() < 5) {
                throw new IllegalArgumentException(
                    format("Case has %s hearing(s), expected at least %s", hearings.size(), 5));
            }

            Element<HearingBooking> hearing1 = hearings.get(3);
            Element<HearingBooking> hearing2 = hearings.get(4);

            if (!expectedHearing1Date.equals(hearing1.getValue().getStartDate().toLocalDate())) {
                throw new IllegalArgumentException(
                    format("Invalid hearing date %s", hearing1.getValue().getStartDate().toLocalDate()));
            }

            if (!expectedHearing2Date.equals(hearing2.getValue().getStartDate().toLocalDate())) {
                throw new IllegalArgumentException(
                    format("Invalid hearing date %s", hearing2.getValue().getStartDate().toLocalDate()));
            }

            if (!HearingType.FURTHER_CASE_MANAGEMENT.equals(hearing1.getValue().getType())) {
                throw new IllegalArgumentException(
                    format("Invalid hearing type %s", hearing1.getValue().getType()));
            }

            if (!HearingType.ISSUE_RESOLUTION.equals(hearing2.getValue().getType())) {
                throw new IllegalArgumentException(
                    format("Invalid hearing type %s", hearing2.getValue().getType()));
            }

            hearings.remove(hearing2);
            hearings.remove(hearing1);

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

    private void run2501(CaseDetails caseDetails) {
        caseDetails.getData().remove("respondents");
        caseDetails.getData().remove("children");
        caseDetails.getData().remove("applicant");
    }

}
