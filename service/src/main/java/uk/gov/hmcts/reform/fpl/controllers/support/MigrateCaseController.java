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
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private static final String FMN_ERROR_MESSAGE = "Unexpected FMN ";

    private final CaseAccessService caseAccessService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2885".equals(migrationId)) {
            run2885(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2885(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (isEmpty(caseData.getCancelledHearingDetails())) {
            throw new IllegalArgumentException("Case does not contain cancelled hearing bookings");
        }

        caseData.getCancelledHearingDetails().forEach(hearingBookingElement -> {
            switch (hearingBookingElement.getValue().getCancellationReason()) {
                case "OT8":
                    hearingBookingElement.getValue().setCancellationReason("IN1");
                    break;
                case "OT9":
                    hearingBookingElement.getValue().setCancellationReason("OT8");
                    break;
                case "OT10":
                    hearingBookingElement.getValue().setCancellationReason("OT9");
                    break;
            }
        });

        caseDetails.getData().put("cancelledHearingDetails", caseData.getCancelledHearingDetails());
    }
}
