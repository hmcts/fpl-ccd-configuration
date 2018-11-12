package uk.gov.hmcts.reform.fpl.service;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;

import java.io.IOException;
import java.util.Map;

/**
 * handle case submission to take in submitted event and add pdf to CCD.
 */
@Component
public class CaseService {

    public static final String JURISDICTION_ID = "PUBLICLAW";
    public static final String CASE_TYPE = "Shared_Storage_DRAFTType";


    @Autowired
    private CoreCaseDataApi coreCaseDataApi;
    @Autowired
    private AuthTokenGenerator authTokenGenerator;
    @Autowired
    private UploadDocumentService uploadDocumentService;
    @Autowired
    private DocumentGeneratorService documentGeneratorService;

    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Uses pdf to upload to ccd.
     *
     * @param event case submitted event.
     * @throws JSONException JSON exception.
     * @throws IOException   Takes in an event of type SubmittedCase.
     */
    @Async
    @EventListener
    public void handleCaseSubmission(SubmittedCaseEvent event) throws JSONException, IOException {
        CallbackRequest request = event.getCallbackRequest();
        String userId = event.getUserId();
        String authorization = event.getAuthorization();

        byte[] pdfDocument = documentGeneratorService.documentGenerator(request.getCaseDetails());

        Document document = uploadDocumentService.uploadDocument(userId, authorization,
            authTokenGenerator.generate(), pdfDocument, getFileName(request.getCaseDetails()));

        logger.debug("binary = " + document.links.binary.href);
        logger.debug("self = " + document.links.self.href);

        String caseId = request.getCaseDetails().getId().toString();

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(authorization,
            authTokenGenerator.generate(), userId, JURISDICTION_ID, CASE_TYPE, caseId, "attachSubmittedFormPDF");

        logger.debug("startEventResponse = " + startEventResponse);
        logger.debug("startEventResponse.token = " + startEventResponse.getToken());

        final Map<String, Object> data = Maps.newHashMap();

        Map<String, Object> documentData = Maps.newHashMap();
        documentData.put("document_url", document.links.self.href);
        documentData.put("document_binary_url", document.links.binary.href);
        documentData.put("document_filename", document.originalDocumentName);
        data.put("submittedForm", documentData);

        CaseDataContent body = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("Attach submitted form")
                .description("Attach submitted form")
                .build())
            .data(data)
            .build();

        coreCaseDataApi.submitEventForCaseWorker(authorization, authTokenGenerator.generate(), userId,
            JURISDICTION_ID, CASE_TYPE, caseId, false, body);
    }

    private String getFileName(CaseDetails caseDetails) {
        try {
            String title = Strings.nullToEmpty(caseDetails.getData().get("caseTitle").toString().trim());
            return title.replaceAll("\\s", "_") + ".pdf";
        } catch (NullPointerException e) {
            return caseDetails.getId().toString() + ".pdf";
        }
    }

}
