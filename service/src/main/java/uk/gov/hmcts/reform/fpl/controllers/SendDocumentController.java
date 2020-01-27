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
import uk.gov.hmcts.reform.fpl.model.DocumentSentToParties;
import uk.gov.hmcts.reform.fpl.model.DocumentsSentToParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocumentHistoryService;
import uk.gov.hmcts.reform.fpl.service.DocumentSenderService;

import java.util.List;

@Api
@RestController
@RequestMapping("/callback/send-document")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendDocumentController {

    private final ObjectMapper mapper;

    private final DocumentSenderService documentSenderService;
    private final DocumentHistoryService documentHistoryService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSave(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        DocumentSentToParties documentSentToParties = printDocument(caseDetails);
        updateSentDocumentsHistory(caseDetails, documentSentToParties);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private DocumentSentToParties printDocument(CaseDetails caseDetails){
        DocumentReference documentToBeSent = mapper.convertValue(caseDetails.getData().remove("documentToBeSent"), DocumentReference.class);
        return documentSenderService.send(documentToBeSent);
    }

    private void updateSentDocumentsHistory(CaseDetails caseDetails, DocumentSentToParties documentSentToParties){
        List<Element<DocumentsSentToParty>> documentsPreviouslySentToParties = mapper.convertValue(caseDetails.getData().get("documentsSentToParties"), new TypeReference<>() {});
        List<Element<DocumentsSentToParty>> updatedDocumentsSentToParties = documentHistoryService.addDocumentSentToParties(documentSentToParties, documentsPreviouslySentToParties);
        caseDetails.getData().put("documentsSentToParties", updatedDocumentsSentToParties);
    }
}
