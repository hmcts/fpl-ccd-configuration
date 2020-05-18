package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocumentSenderService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.SentDocumentHistoryService;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;

@Api
@RestController
@RequestMapping("/callback/send-document")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendDocumentController {
    private static final String DOCUMENT_TO_BE_SENT_KEY = "documentToBeSent";
    private final ObjectMapper mapper;
    private final DocumentSenderService documentSenderService;
    private final SentDocumentHistoryService sentDocumentHistoryService;
    private final RepresentativeService representativeService;
    private final FeatureToggleService featureToggleService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSave(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        var representativesServedByPost =
            representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), POST);

        if (featureToggleService.isXeroxPrintingEnabled() && !representativesServedByPost.isEmpty()) {
            DocumentReference documentToBeSent = mapper.convertValue(caseDetails.getData()
                .get(DOCUMENT_TO_BE_SENT_KEY), DocumentReference.class);

            List<SentDocument> sentDocuments = documentSenderService.send(documentToBeSent,
                representativesServedByPost,
                caseDetails.getId(),
                caseData.getFamilyManCaseNumber());

            updateSentDocumentsHistory(caseDetails, sentDocuments);
        }
        caseDetails.getData().remove(DOCUMENT_TO_BE_SENT_KEY);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private void updateSentDocumentsHistory(CaseDetails caseDetails, List<SentDocument> sentDocuments) {
        List<Element<SentDocuments>> sentDocumentsHistory = mapper
            .convertValue(caseDetails.getData().get("documentsSentToParties"), new TypeReference<>() {
            });

        caseDetails.getData().put("documentsSentToParties",
            sentDocumentHistoryService.addToHistory(sentDocumentsHistory, sentDocuments));
    }
}
