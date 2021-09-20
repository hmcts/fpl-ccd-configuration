package uk.gov.hmcts.reform.fpl.model.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.stream.Collectors;

public interface ApplicationsBundle extends ConfidentialBundle {

    String toLabel();

    int getSortOrder();

    String getUploadedDateTime();

    DocumentReference getApplication();

    @JsonIgnore
    default String getSupportingEvidenceFileNames() {
        return getSupportingEvidenceBundle().stream()
            .map(Element::getValue)
            .map(SupportingEvidenceBundle::getDocument)
            .map(DocumentReference::getFilename)
            .collect(Collectors.joining("\n"));
    }

    @JsonIgnore
    default List<Element<DocumentReference>> getSupportingEvidenceBundleReferences() {
        return getSupportingEvidenceBundle().stream()
            .map(Element::getValue)
            .map(SupportingEvidenceBundle::getDocument)
            .map(ElementUtils::element)
            .collect(Collectors.toList());
    }
}
