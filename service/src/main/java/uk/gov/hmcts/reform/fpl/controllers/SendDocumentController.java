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
import uk.gov.hmcts.reform.fpl.model.DocumentsSentToParty;
import uk.gov.hmcts.reform.fpl.model.PrintedDocument;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocumentHistoryService;
import uk.gov.hmcts.reform.fpl.service.DocumentSenderService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;

@Api
@RestController
@RequestMapping("/callback/send-document")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendDocumentController {

    private final ObjectMapper mapper;

    private final DocumentSenderService documentSenderService;
    private final DocumentHistoryService documentHistoryService;
    private final RepresentativeService representativeService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSave(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Representative> representativesServedByPost =
            representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), POST);
        String familyManCaseNumber = caseData.getFamilyManCaseNumber();

        if (!representativesServedByPost.isEmpty()) {
            DocumentReference documentToBeSent = mapper.convertValue(caseDetails.getData().remove("documentToBeSent"),
                DocumentReference.class);
            List<PrintedDocument> printedDocuments =
                printDocuments(documentToBeSent, caseDetails.getId(), familyManCaseNumber, representativesServedByPost);
            updateSentDocumentsHistory(caseDetails, printedDocuments);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private List<PrintedDocument> printDocuments(DocumentReference documentToBeSent,
                                                 Long ccdCaseNumber,
                                                 String familyManCaseNumber,
                                                 List<Representative> representativesServedByPost) {
        return documentSenderService.send(documentToBeSent, ccdCaseNumber, familyManCaseNumber,
            representativesServedByPost);
    }

    private void updateSentDocumentsHistory(CaseDetails caseDetails, List<PrintedDocument> printedDocuments) {
        List<Element<DocumentsSentToParty>> documentsPreviouslySentToParty = mapper.convertValue(
            caseDetails.getData().get("documentsSentToPartyCollection"), new TypeReference<>() {});
        List<Element<DocumentsSentToParty>> updatedDocumentsSentToParty =
            documentHistoryService.updateDocumentsSentToPartyCollection(
                printedDocuments, documentsPreviouslySentToParty);
        caseDetails.getData().put("documentsSentToPartyCollection", updatedDocumentsSentToParty);
    }
}
