package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;
import uk.gov.hmcts.reform.pdf.generator.HTMLToPDFConverter;

import java.util.Map;
import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping("/callback/case-submission")
public class CaseSubmissionController {

    private final HTMLToPDFConverter converter = new HTMLToPDFConverter();
    @Autowired
    private final DocumentTemplates documentTemplates = new DocumentTemplates();

    @PostMapping
    public ResponseEntity submittedCase(@RequestBody @NotNull Map<String,
        Object> caseData) {
        byte[] template = documentTemplates.getHtmlTemplate();
        byte[] pdfDocument = converter.convert(template, caseData);
        return new ResponseEntity(HttpStatus.OK);
    }
}
