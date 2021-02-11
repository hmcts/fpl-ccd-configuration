package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialBundle;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class C2DocumentBundle extends ConfidentialBundle {
    private final C2ApplicationType type;
    private final String nameOfRepresentative;
    private final String usePbaPayment;
    private final String pbaNumber;
    private final String clientCode;
    private final String fileReference;
    private final DocumentReference document;
    private final String description;
    private final String uploadedDateTime;
    private final String author;
    private List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;

    public String toLabel(int index) {
        return format("Application %d: %s", index, uploadedDateTime);
    }

    @Override
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle() {
        return defaultIfNull(supportingEvidenceBundle, new ArrayList<>());
    }

    @JsonIgnore
    public String getAllC2DocumentFileNames() {
        String c2Filename = "";

        if (document != null) {
            c2Filename = document.getFilename();
        }

        String stringBuilder = c2Filename + "\n" + getSupportingEvidenceFileNames();
        return stringBuilder.trim();
    }

    @JsonIgnore
    public List<Element<DocumentReference>> getAllC2DocumentReferences() {
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
