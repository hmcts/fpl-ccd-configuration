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
import uk.gov.hmcts.reform.fpl.model.DocumentSentToParty;
import uk.gov.hmcts.reform.fpl.model.DocumentsSentToParty;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocumentSenderService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.SentDocumentHistoryService;

import java.util.List;

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

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSave(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Representative> partiesServedByPost =
            representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), POST);

        if (!partiesServedByPost.isEmpty()) {
            DocumentReference documentToBeSent = mapper.convertValue(caseDetails.getData().remove("documentToBeSent"),
                DocumentReference.class);
            List<DocumentSentToParty> printedDocuments =
                printDocuments(documentToBeSent, partiesServedByPost);
            updateSentDocumentsHistory(caseDetails, printedDocuments);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private List<DocumentSentToParty> printDocuments(DocumentReference documentToBeSent,
                                                     List<Representative> representativesServedByPost) {
        return documentSenderService.send(documentToBeSent, representativesServedByPost);
    }

    private void updateSentDocumentsHistory(CaseDetails caseDetails, List<DocumentSentToParty> sentDocuments) {
        List<Element<DocumentsSentToParty>> sentDocumentsHistory = mapper
            .convertValue(caseDetails.getData().get("documentsSentToParties"), new TypeReference<>() {
            });

        caseDetails.getData().put("documentsSentToParties",
            documentHistoryService.addToHistory(sentDocumentsHistory, sentDocuments));
    }
}
