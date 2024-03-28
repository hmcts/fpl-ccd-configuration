package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.service.SendLetterService;
import uk.gov.hmcts.reform.fpl.service.SentDocumentHistoryService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;

/*
@deprecated - use SendDocumentService instead
*/

@Deprecated
@Slf4j
@RestController
@RequestMapping("/callback/send-document")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendDocumentController extends CallbackController {
    private static final String DOCUMENT_TO_BE_SENT_KEY = "documentToBeSent";
    private final ObjectMapper mapper;
    private final SendLetterService sendLetterService;
    private final SentDocumentHistoryService sentDocumentHistoryService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSave(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<Recipient> representativesServedByPost =
            new ArrayList<>(caseData.getRepresentativesByServedPreference(POST));

        if (!representativesServedByPost.isEmpty()) {
            DocumentReference documentToBeSent = mapper.convertValue(caseDetails.getData()
                .get(DOCUMENT_TO_BE_SENT_KEY), DocumentReference.class);
            log.info("Sending to representatives served by POST for case {}", caseDetails.getId());

            List<SentDocument> sentDocuments = sendLetterService.send(documentToBeSent,
                representativesServedByPost,
                caseDetails.getId(),
                caseData.getFamilyManCaseNumber(), Language.ENGLISH);

            updateSentDocumentsHistory(caseDetails, sentDocuments);
        } else {
            log.info("There are no representatives served by POST for case {}", caseDetails.getId());
        }
        caseDetails.getData().remove(DOCUMENT_TO_BE_SENT_KEY);

        return respond(caseDetails);
    }

    private void updateSentDocumentsHistory(CaseDetails caseDetails, List<SentDocument> sentDocuments) {
        List<Element<SentDocuments>> sentDocumentsHistory = mapper
            .convertValue(caseDetails.getData().get("documentsSentToParties"), new TypeReference<>() {
            });

        caseDetails.getData().put("documentsSentToParties",
            sentDocumentHistoryService.addToHistory(sentDocumentsHistory, sentDocuments));
    }
}
