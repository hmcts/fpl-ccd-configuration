package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;
import uk.gov.hmcts.reform.pdf.generator.HTMLTemplateProcessor;
import uk.gov.hmcts.reform.pdf.generator.HTMLToPDFConverter;
import uk.gov.hmcts.reform.pdf.generator.PDFGenerator;
import uk.gov.hmcts.reform.pdf.generator.XMLContentSanitizer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;


@Component
public class CaseService {

    public static final String JURISDICTION_ID = "PUBLICLAW";
    public static final String CASE_TYPE = "Shared_Storage_DRAFTType";
    private final HTMLToPDFConverter converter = new HTMLToPDFConverter();

    @Autowired
    private DocumentTemplates documentTemplates;
    @Autowired
    private DocumentUploadClientApi documentUploadClient;
    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    /**
     *
     * @param authorization
     * @param serviceAuthorization
     * @param userId
     * @param request
     * @throws JSONException
     * @throws IOException
     *
     * Converts request @param to a map that contains the case data and the headers that can be used to create the pdf and view contents of the
     * callback
     */
    @Async
    @SuppressWarnings("unchecked")
    public void handleCaseSubmission(
        String authorization,
        String serviceAuthorization,
        String userId,
        CallbackRequest request) throws JSONException, IOException {
        byte[] template = documentTemplates.getHtmlTemplate();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.convertValue(request.getCaseDetails(), Map.class);

        System.out.println("map = " + map);
        byte[] pdfDocument = converter.convert(template, map);

        Document document = uploadDocument(userId, authorization, serviceAuthorization, pdfDocument, getFileName(request.getCaseDetails()));

        System.out.println("binary = " + document.links.binary.href);
        System.out.println("self = " + document.links.self.href);

        String caseId = request.getCaseDetails().getId().toString();

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(authorization, serviceAuthorization, userId, JURISDICTION_ID,
            CASE_TYPE, caseId,"PDF");

        System.out.println("startEventResponse = " + startEventResponse);
        System.out.println("startEventResponse.token = " + startEventResponse.getToken());

        Map<String, Object> data = Maps.newHashMap();

        Map<String, Object> documentData = Maps.newHashMap();
        documentData.put("document_url", document.links.self.href.replace("dm-store:8080", "localhost:3453"));
        documentData.put("document_binary_url", document.links.binary.href.replace("dm-store:8080", "localhost:3453"));
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

        CaseDetails caseDetails = coreCaseDataApi.submitEventForCaseWorker(authorization,
            serviceAuthorization, userId, JURISDICTION_ID, CASE_TYPE, caseId, false, body);
    }

    private Document uploadDocument(String userId, String authorization,
                                    String serviceAuthorization, byte[] pdfDocument, String fileName) {
        MultipartFile file = new InMemoryMultipartFile("files", fileName, MediaType.APPLICATION_PDF_VALUE, pdfDocument);

        UploadResponse response = documentUploadClient.upload(authorization, serviceAuthorization,
            userId, newArrayList(file));
        System.out.println("response = " + response);

        return response.getEmbedded().getDocuments().stream()
            .findFirst()
            .orElseThrow(() ->
                new RuntimeException("Document management failed uploading"));
    }

    private String getFileName(CaseDetails caseDetails) {
        try {
            String title = Strings.nullToEmpty(caseDetails.getData().get("caseTitle").toString().trim());
            return title.replaceAll("\\s", "_") + ".pdf";
        } catch (NullPointerException e){
            return caseDetails.getId().toString() + ".pdf";
        }
    }

}
