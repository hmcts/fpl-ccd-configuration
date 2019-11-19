package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.MandatoryDocuments;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.validation.groups.UploadDocumentsGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Service
public class DocumentsValidatorService {
    private final Validator validator;

    @Autowired
    DocumentsValidatorService(Validator validator) {
        this.validator = validator;
    }

    // Moved from SocialWorkOtherSubmissionController
    public List<String> validateSocialWorkOtherDocuments(List<Element<DocumentSocialWorkOther>> socialWorkOtherData) {
        List<String> validationErrors = new ArrayList<String>();

        if (isNotEmpty(socialWorkOtherData)) {
            AtomicInteger i = new AtomicInteger(1);
            socialWorkOtherData.forEach(element -> {
                DocumentSocialWorkOther document = element.getValue();

                if (isEmpty(document.getDocumentTitle())) {
                    validationErrors.add(String.format("You must give additional document %s a name.", i.get()));
                }

                i.getAndIncrement();
            });
        }

        return validationErrors;
    }

    public List<String> validateDocuments(CaseData caseData) {
        Set<ConstraintViolation<CaseData>> violations = validator.validate(caseData, UploadDocumentsGroup.class);

        return Stream.of(MandatoryDocuments.values())
            .flatMap(section -> formatDocumentViolations(violations, section))
            .collect(Collectors.toCollection(() ->
                validateSocialWorkOtherDocuments(caseData.getOtherSocialWorkDocuments())));
    }

    private Stream<String> formatDocumentViolations(Set<ConstraintViolation<CaseData>> constraintViolations,
                                         MandatoryDocuments document) {
        return constraintViolations.stream()
            .filter(error -> error.getPropertyPath().toString().contains(document.getPropertyKey()))
            .map(error -> String.format("Check document %d. %s", document.getInterfaceDisplayOrder(),
                error.getMessage()))
            .distinct();
    }
}
