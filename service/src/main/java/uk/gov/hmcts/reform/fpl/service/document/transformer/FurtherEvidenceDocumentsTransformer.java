package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Component
public class FurtherEvidenceDocumentsTransformer {

    public List<DocumentView> getFurtherEvidenceDocumentsView(
        FurtherEvidenceType type,
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments,
        boolean includeConfidential) {

        return nullSafeList(furtherEvidenceDocuments)
            .stream()
            .map(Element::getValue)
            .filter(doc -> (type == doc.getType()) && (includeConfidential || !doc.isConfidentialDocument()))
            .sorted(comparing(SupportingEvidenceBundle::getDateTimeUploaded, nullsLast(reverseOrder())))
            .map(doc -> DocumentView.builder()
                .document(doc.getDocument())
                .type(doc.getType().getLabel())
                .fileName(doc.getName())
                .uploadedDateTime(doc.getDateTimeUploaded())
                .uploadedAt(isNotEmpty(doc.getDateTimeUploaded())
                    ? formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE) : null)
                .uploadedBy(doc.getUploadedBy())
                .documentName(doc.getName())
                .confidential(doc.isConfidentialDocument())
                .title(doc.getName())
                .includeDocumentName(asList(APPLICANT_STATEMENT, OTHER).contains(doc.getType()))
                .build())
            .collect(Collectors.toUnmodifiableList());
    }

}
