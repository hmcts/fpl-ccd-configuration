package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;
import uk.gov.hmcts.reform.pdf.generator.HTMLToPDFConverter;
import javax.validation.constraints.NotNull;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

@Api
@RestController
@RequestMapping("/callback/case-submission")
public class CaseSubmissionController {

    private final HTMLToPDFConverter converter = new HTMLToPDFConverter();

    @Autowired
    private DocumentTemplates documentTemplates;
    @Autowired
    private DocumentUploadClientApi documentUploadClient;

    @PostMapping
    public ResponseEntity submittedCase(
        @RequestHeader(value = "serviceauthorization") String serviceAuthorization,
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody @NotNull Map<String, Object> caseData) {
        System.out.println("Service authorization: " + serviceAuthorization);
        System.out.println("Authorization: " + authorization);
        System.out.println("User Id: " + userId);
        byte[] template = documentTemplates.getHtmlTemplate();
        byte[] pdfDocument = converter.convert(template, caseData);

        Document document = uploadDocument(userId, authorization, serviceAuthorization, pdfDocument);

        System.out.println("binary = " + document.links.binary.href);
        System.out.println("self = " + document.links.self.href);

        return new ResponseEntity(HttpStatus.OK);
    }

    private Document uploadDocument(String userId, String authorization, String serviceAuthorization, byte[] pdfDocument) {
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
