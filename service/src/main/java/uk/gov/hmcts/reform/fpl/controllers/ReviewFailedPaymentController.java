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
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static java.lang.String.format;

@Api
@Slf4j
@RestController
@RequestMapping("/callback/review-failed-payment")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewFailedPaymentController extends CallbackController {

    //private final SendOrderReminderService sendOrderReminderService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        boolean hasHearingsMissingOrders = !isEmpty(caseData.getFailedPayments());

        caseDetails.getData().put("hasToBeReviewedFailedPayments", YesNo.from(hasHearingsMissingOrders));
        if (hasHearingsMissingOrders) {
            List<DynamicListElement> listItems = new ArrayList<>();
            caseData.getFailedPayments().forEach(fp -> {
                String applicationTypeString = "";
                List<ApplicationType> applicationTypes =
                    Optional.ofNullable(fp.getValue().getApplicationTypes()).orElse(List.of());
                if (!applicationTypes.isEmpty()) {
                    applicationTypeString += applicationTypes.get(0).getType();
                    if (applicationTypes.size() > 1) {
                        applicationTypeString += format(" [+{} more]", applicationTypes.size() - 1);
                    }
                }
                listItems.add(DynamicListElement.builder()
                    .code(fp.getId())
                    .label(fp.getValue().getPaymentAt() + ": "
                        + applicationTypeString + " by "
                        + fp.getValue().getOrderApplicantName())
                    .build());
                caseDetails.getData().put("listOfToBeReviewedFailedPayments", DynamicList.builder()
                        .listItems(listItems)
                    .build());
            });
        }
        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
    }

}