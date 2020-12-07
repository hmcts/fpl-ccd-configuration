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

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static java.lang.String.format;

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


        if ("FPLA-2469".equals(migrationId)) {
            run2469(caseDetails);
        }
        if ("FPLA-2501".equals(migrationId)) {
            run2501(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }


    private void run2469(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("SN20C50010".equals(caseData.getFamilyManCaseNumber())) {
            List<Element<HearingBooking>> hearings = caseData.getHearingDetails();

            int hearingIndex = 0;
            LocalDate expectedHearingDate = LocalDate.of(2020, Month.OCTOBER, 14);

            if (hearings.size() < hearingIndex + 1) {
                throw new IllegalArgumentException(format("Case %s has %s hearing(s), expected at least %s ",
                    caseDetails.getId(), hearings.size(), hearingIndex + 1));
            }

            LocalDate hearingDate = hearings.get(hearingIndex).getValue().getStartDate().toLocalDate();

            if (!expectedHearingDate.equals(hearingDate)) {
                throw new IllegalArgumentException(format("Invalid hearing date %s", hearingDate));
            }

            hearings.remove(hearingIndex);

            caseDetails.getData().put("hearingDetails", hearings);
        }
    }

    private void run2501(CaseDetails caseDetails) {
        caseDetails.getData().remove("respondents");
    }
}
