package uk.gov.hmcts.reform.fpl.templates;

import org.springframework.stereotype.Component;

import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@Component
public class DocumentTemplates {

    public byte[] getHtmlTemplate() {
        return readBytes("HTMLTemplate.html");
    }

}
