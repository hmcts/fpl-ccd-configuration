package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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

import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

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

        if ("FPLA-2640".equals(migrationId)) {
            run2640(caseDetails);
        }

        if ("FPLA-2651".equals(migrationId)) {
            run2651(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2640(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("NE20C50006".equals(caseData.getFamilyManCaseNumber())) {

            if (isEmpty(caseData.getDraftUploadedCMOs())) {
                throw new IllegalStateException("No draft case management orders in the case");
            }

            caseData.getDraftUploadedCMOs().remove(0);

            if (isEmpty(caseData.getDraftUploadedCMOs())) {
                caseDetails.getData().remove("draftUploadedCMOs");
            } else {
                caseDetails.getData().put("draftUploadedCMOs", caseData.getDraftUploadedCMOs());
            }
        }
    }

    private void run2651(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("NE21C50001".equals(caseData.getFamilyManCaseNumber())) {
            List<Element<HearingBooking>> hearings = caseData.getHearingDetails();

            if (ObjectUtils.isEmpty(hearings)) {
                throw new IllegalArgumentException("No hearings in the case");
            }

            Element<HearingBooking> hearingToBeRemoved = hearings.get(0);

            hearings.remove(hearingToBeRemoved);

            caseDetails.getData().put("hearingDetails", hearings);
        }
    }
}
