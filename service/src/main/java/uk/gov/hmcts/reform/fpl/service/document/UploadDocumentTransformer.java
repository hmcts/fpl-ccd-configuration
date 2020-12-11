package uk.gov.hmcts.reform.fpl.service.document;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.IdentityService;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.OTHER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadDocumentTransformer {

    private final IdentityService identityService;

    public Element<ApplicationDocument> convert(Document document, ApplicationDocumentType documentType) {
        return element(identityService.generateId(),
            ApplicationDocument.builder()
                .document(document.getTypeOfDocument())
                .dateTimeUploaded(document.getDateTimeUploaded())
                .uploadedBy(document.getUploadedBy())
                .documentType(documentType)
                .includedInSWET(null)
                .build());
    }

    public Element<CourtBundle> convert(CourtBundle document) {
        return element(identityService.generateId(), document);
    }

    public List<Element<ApplicationDocument>> convert(List<Element<DocumentSocialWorkOther>> otherSocialWorkDocuments) {
        return otherSocialWorkDocuments.stream().map(
            element -> {
                DocumentSocialWorkOther document = element.getValue();
                return element(element.getId(),
                    ApplicationDocument.builder()
                        .document(document.getTypeOfDocument())
                        .dateTimeUploaded(document.getDateTimeUploaded())
                        .uploadedBy(document.getUploadedBy())
                        .documentType(OTHER)
                        .documentName(document.getDocumentTitle())
                        .build());
            }
        ).collect(Collectors.toList());

    }
}
