package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.SubmittedFormFilenameHelper.buildFileName;

@Api
@RestController
@RequestMapping("/callback/populate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestPopulateCaseController {
    private final ObjectMapper mapper;
    private final DocumentGeneratorService documentGeneratorService;
    private final UploadDocumentService uploadDocumentService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        //TODO: get filename from case data and remove it afterwards
//        String filename = "mandatorySubmissionFields";
        String filename = data.get("caseDataFilename").toString();

        List<String> errors = new ArrayList<>();
        try {
            data.putAll(readFileData(filename));
        } catch (Exception e) {
            errors.add(String.format("Could not read file %s", filename));
        }

        //TODO: fill mandatory fields with valid data (UIDs, datetime etc.)

        //TODO: upload submittedForm to have valid and working document in the case
        //        byte[] pdf = documentGeneratorService.generateSubmittedFormPDF(caseDetails,
        //            Pair.of("userFullName", "kurt@swansea.gov.uk (local-authority)")
        //        );
        //        var document = uploadDocumentService.uploadPDF(pdf, buildFileName(caseDetails));
        //        data.put("submittedForm", DocumentReference.buildFromDocument(document));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .errors(errors)
            .build();
    }

    private Map<String, Object> readFileData(String filename) throws Exception {
        String filePath = String.format("e2e/fixtures/%s.json", filename);
        String jsonContent = ResourceReader.readString(filePath);

        return mapper.readValue(jsonContent, new TypeReference<>() {});
    }
}
