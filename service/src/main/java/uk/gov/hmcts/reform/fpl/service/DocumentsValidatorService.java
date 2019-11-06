package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.MandatoryDocuments;
import uk.gov.hmcts.reform.fpl.interfaces.UploadDocumentsGroup;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.MandatoryDocuments.CHECKLIST;
import static uk.gov.hmcts.reform.fpl.enums.MandatoryDocuments.SOCIAL_WORK_ASSESSMENT;
import static uk.gov.hmcts.reform.fpl.enums.MandatoryDocuments.SOCIAL_WORK_CARE_PLAN;
import static uk.gov.hmcts.reform.fpl.enums.MandatoryDocuments.SOCIAL_WORK_CHRONOLOGY;
import static uk.gov.hmcts.reform.fpl.enums.MandatoryDocuments.SOCIAL_WORK_EVIDENCE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.MandatoryDocuments.SOCIAL_WORK_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.MandatoryDocuments.THRESHOLD;

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

        List<String> validationErrors = Stream.of(SOCIAL_WORK_CHRONOLOGY, SOCIAL_WORK_STATEMENT, SOCIAL_WORK_ASSESSMENT,
            SOCIAL_WORK_CARE_PLAN, SOCIAL_WORK_EVIDENCE_TEMPLATE, THRESHOLD, CHECKLIST)
            .flatMap(section -> Stream.of(formatDocumentViolations(violations, section)))
            .flatMap(Collection::stream)
            .collect(toList());

        validationErrors.addAll(validateSocialWorkOtherDocuments(caseData.getOtherSocialWorkDocuments()));

        return validationErrors;
    }

    private List<String> formatDocumentViolations(Set<ConstraintViolation<CaseData>> constraintViolations,
                                         MandatoryDocuments document) {
        List<String> errorList;

        errorList = constraintViolations.stream()
            .filter(error -> error.getPropertyPath().toString().contains(document.getPropertyKey()))
            .map(error -> String.format("Check document %s. %s", document.getInterfaceDisplayOrder(),
                error.getMessage()))
            .distinct()
            .collect(Collectors.toList());

        return errorList;
    }
}
