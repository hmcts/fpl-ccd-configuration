package uk.gov.hmcts.reform.fpl.service.translations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DocumentWordCounter {

    private final DocumentConversionService documentConversionService;
    private final DocmosisHelper docmosisHelper;

    public long count(byte[] originalDocumentContent) {

        byte[] pdfFile = documentConversionService.convertToPdf(originalDocumentContent, "toCalculate.pdf");

        String rawContent = docmosisHelper.extractPdfContent(pdfFile);

        return Stream.of(rawContent.split("[ ,.;!?\r\n]")).filter(s -> s.length() > 0).count();

    }
}
