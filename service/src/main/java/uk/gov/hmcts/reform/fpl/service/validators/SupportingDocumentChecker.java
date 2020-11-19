package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class SupportingDocumentChecker implements EventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return emptyList();
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        final List<Element<SupportingDocument>> supportingDocuments = caseData.getDocuments();

        if (isEmpty(supportingDocuments)) {
            return false;
        }

        SupportingDocument anyDocument = supportingDocuments.stream().filter(Objects::nonNull).findAny().get().getValue();

        return anyNonEmpty(
            anyDocument.getDocument(),
            anyDocument.getDocumentType());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        return false;
    }
}
