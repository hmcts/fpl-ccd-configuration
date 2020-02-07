package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocumentSenderService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.SentDocumentHistoryService;

import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;

@Api
@RestController
@RequestMapping("/callback/send-document")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendDocumentController {

    private final ObjectMapper mapper;

    private final DocumentSenderService documentSenderService;
    private final SentDocumentHistoryService documentHistoryService;
    private final RepresentativeService representativeService;
    private final FeatureToggleService featureToggleService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSave(
        @RequestHeader("authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        var representativesServedByPost =
            representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), POST);
        if (featureToggleService.isXeroxPrintingEnabled() && !representativesServedByPost.isEmpty()) {
            DocumentReference documentToBeSent = mapper.convertValue(caseDetails.getData()
                .remove("documentToBeSent"), DocumentReference.class);

            var printedDocuments = documentSenderService.send(documentToBeSent,
                representativesServedByPost,
                authorization,
                userId);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
