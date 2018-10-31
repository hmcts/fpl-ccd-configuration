package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;
import uk.gov.hmcts.reform.pdf.generator.HTMLToPDFConverter;
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

    @Async
    @SuppressWarnings("unchecked")
    public void handleCaseSubmission(
        String authorization,
        String serviceAuthorization,
        String userId,
        Map<String, Object> caseData) {
        byte[] template = documentTemplates.getHtmlTemplate();
        byte[] pdfDocument = converter.convert(template, caseData);

        Document document = uploadDocument(userId, authorization, serviceAuthorization, pdfDocument);

        System.out.println("binary = " + document.links.binary.href);
        System.out.println("self = " + document.links.self.href);

        Map<String, Object> case_details = (Map<String, Object>) caseData.get("case_details");
        Long caseId = (Long) case_details.get("id");

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(authorization,
            serviceAuthorization, userId, JURISDICTION_ID,
            CASE_TYPE, caseId.toString(),
            "PDF");

        System.out.println("startEventResponse = " + startEventResponse);
        System.out.println("startEventResponse.token = " + startEventResponse
            .getToken());

        Map<String, Object> data = Maps.newHashMap();

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

        CaseDetails caseDetails = coreCaseDataApi.submitEventForCaseWorker(authorization,
            serviceAuthorization, userId, JURISDICTION_ID, CASE_TYPE, caseId
                .toString(), false, body);

        System.out.println("caseDetails = " + caseDetails);
    }

    private Document uploadDocument(String userId, String authorization,
                                    String serviceAuthorization, byte[] pdfDocument) {
        MultipartFile file = new InMemoryMultipartFile("files",
            "Test.pdf",
            MediaType.APPLICATION_PDF_VALUE, pdfDocument);

        UploadResponse response = documentUploadClient.upload(authorization, serviceAuthorization,
            userId, newArrayList(file));
        System.out.println("response = " + response);

        return response.getEmbedded().getDocuments().stream()
            .findFirst()
            .orElseThrow(() ->
                new RuntimeException("Document management failed uploading"));
    }

}
