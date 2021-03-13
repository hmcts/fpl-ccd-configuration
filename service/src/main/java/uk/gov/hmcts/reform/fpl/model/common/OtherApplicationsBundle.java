package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType;
import uk.gov.hmcts.reform.fpl.model.SupplementsBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialBundle;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OtherApplicationsBundle implements ConfidentialBundle {
    private final OtherApplicationType applicationType;
    private final ParentalResponsibilityType parentalResponsibilityType;
    private final DocumentReference document;
    private final String uploadedDateTime;
    private final String author;
    private List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;
    private List<Element<SupplementsBundle>> supplementsBundle;

    @Override
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle() {
        return defaultIfNull(supportingEvidenceBundle, new ArrayList<>());
    }

    public List<Element<SupplementsBundle>> getSupplementsBundle() {
        return defaultIfNull(supplementsBundle, new ArrayList<>());
    }

    @Override
    @JsonIgnore
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceLA() {
        return getSupportingEvidenceBundle().stream()
            .filter(doc -> !(doc.getValue().isUploadedByHMCTS() && doc.getValue().isConfidentialDocument()))
            .collect(Collectors.toList());
    }

    @Override
    @JsonIgnore
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceNC() {
        return getSupportingEvidenceBundle().stream()
            .filter(doc -> !doc.getValue().isConfidentialDocument())
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public String getAllDocumentFileNames() {
        String c2Filename = "";

        if (document != null) {
            c2Filename = document.getFilename();
        }

        String stringBuilder = c2Filename + "\n" + getSupportingEvidenceFileNames();
        return stringBuilder.trim();
    }

    @JsonIgnore
    public List<Element<DocumentReference>> getAllDocumentReferences() {
        List<Element<DocumentReference>> documentReferences = new ArrayList<>();

        if (document != null) {
            documentReferences.add(element(document));
        }

        documentReferences.addAll(getSupportingEvidenceBundleReferences());

        return documentReferences;
    }

    @JsonIgnore
    private String getSupportingEvidenceFileNames() {
        return getSupportingEvidenceBundle().stream()
            .map(Element::getValue)
            .map(SupportingEvidenceBundle::getDocument)
            .map(DocumentReference::getFilename)
            .collect(Collectors.joining("\n"));
    }

    @JsonIgnore
    private List<Element<DocumentReference>> getSupportingEvidenceBundleReferences() {
        return getSupportingEvidenceBundle().stream()
            .map(Element::getValue)
            .map(SupportingEvidenceBundle::getDocument)
            .map(ElementUtils::element)
            .collect(Collectors.toList());
    }
}
