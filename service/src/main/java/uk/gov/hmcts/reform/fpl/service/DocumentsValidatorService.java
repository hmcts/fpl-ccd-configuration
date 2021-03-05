package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Service
public class DocumentsValidatorService {
    // Moved from SocialWorkOtherSubmissionController
    public List<String> validateSocialWorkOtherDocuments(List<Element<DocumentSocialWorkOther>> socialWorkOtherData) {
        List<String> validationErrors = new ArrayList<>();

        if (isNotEmpty(socialWorkOtherData)) {
            AtomicInteger i = new AtomicInteger(1);
            socialWorkOtherData.forEach(element -> {
                DocumentSocialWorkOther document = element.getValue();

                if (isEmpty(document.getDocumentTitle())) {
                    validationErrors.add(String.format("You must give additional document %s a name.", i.get()));
                }
                if (isNull(document.getTypeOfDocument())) {
                    validationErrors.add(String.format("You must upload a file for additional document %s.", i.get()));
                }

                i.getAndIncrement();
            });
        }

        return validationErrors;
    }
}
