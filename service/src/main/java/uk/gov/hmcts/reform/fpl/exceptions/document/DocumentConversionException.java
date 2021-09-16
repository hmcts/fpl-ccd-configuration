package uk.gov.hmcts.reform.fpl.exceptions.document;

import static java.lang.String.format;

public class DocumentConversionException extends RuntimeException {
    public DocumentConversionException(String extension, Exception cause) {
        super(format("Could not convert document of type \"%s\" to pdf", extension), cause);
    }
}
