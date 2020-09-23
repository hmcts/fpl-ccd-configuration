package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.AmendedReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.casesubmission.CaseSubmissionService;
import uk.gov.hmcts.reform.fpl.service.markdown.CaseSubmissionMarkdownService;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;
import uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInOpenState;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInReturnedState;

@Api
@RestController
@RequestMapping("/callback/case-submission")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseSubmissionController extends CallbackController {
    private static final String DISPLAY_AMOUNT_TO_PAY = "displayAmountToPay";
    private static final String CONSENT_TEMPLATE = "I, %s, believe that the facts stated in this application are true.";
    private final CaseSubmissionService caseSubmissionService;
    private final FeeService feeService;
    private final FeatureToggleService featureToggleService;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final CaseSubmissionMarkdownService markdownService;
    private final CaseSubmissionChecker caseSubmissionChecker;
    private final IdamClient idamClient;
    private final RequestData requestData;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        data.remove(DISPLAY_AMOUNT_TO_PAY);

        Document document = caseSubmissionService.generateSubmittedFormPDF(caseData, true);
        data.put("draftApplicationDocument", buildFromDocument(document));

        if (isInOpenState(caseDetails)) {
            try {
                FeesData feesData = feeService.getFeesDataForOrders(caseData.getOrders());
                data.put("amountToPay", BigDecimalHelper.toCCDMoneyGBP(feesData.getTotalAmount()));
                data.put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
            } catch (FeeRegisterException ignore) {
                data.put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());
            }
        }

        String label = String.format(CONSENT_TEMPLATE, idamClient.getUserInfo(requestData.authorisation())
            .getName());
        data.put("submissionConsentLabel", label);

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);
        final List<String> errors = caseSubmissionChecker.validate(caseData);

        return respond(callbackRequest.getCaseDetails(), errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = validate(caseData);

        if (errors.isEmpty()) {
            Document document = caseSubmissionService.generateSubmittedFormPDF(caseData, false);

            ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

            Map<String, Object> data = caseDetails.getData();
            data.put("dateAndTimeSubmitted", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime));
            data.put("dateSubmitted", DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));
            data.put("sendToCtsc", setSendToCtsc(data.get("caseLocalAuthority").toString()).getValue());
            data.put("submittedForm", ImmutableMap.<String, String>builder()
                .put("document_url", document.links.self.href)
                .put("document_binary_url", document.links.binary.href)
                .put("document_filename", document.originalDocumentName)
                .build());
        }

        return respond(caseDetails, errors);
    }

    @PostMapping("/submitted")
    public SubmittedCallbackResponse handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);
        if (caseDataBefore.getState() == OPEN) {
            publishEvent(new SubmittedCaseEvent(caseData, caseDataBefore));
            publishEvent(new CaseDataChanged(caseData));
        } else if (isInReturnedState(callbackRequest.getCaseDetailsBefore())) {
            publishEvent(new AmendedReturnedCaseEvent(caseData));
        }

        MarkdownData markdownData = markdownService.getMarkdownData(caseData.getCaseName());

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(markdownData.getHeader())
            .confirmationBody(markdownData.getBody())
            .build();
    }

    private YesNo setSendToCtsc(String caseLocalAuthority) {
        String localAuthorityName = localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseLocalAuthority);

        return YesNo.from(featureToggleService.isCtscEnabled(localAuthorityName));
    }

    private List<String> validate(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        if (featureToggleService.isRestrictedFromCaseSubmission(caseData.getCaseLocalAuthority())) {
            errors.add("You cannot submit this application online yet."
                + " Ask your FPL administrator for your local authority’s enrolment date");
        }

        return errors;
    }

}
