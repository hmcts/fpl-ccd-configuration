package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class C2DocumentBundle {
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

    @JsonIgnore
    public String getC2DocumentFileNames() {
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
        StringBuilder stringBuilder = new StringBuilder();

        if (supportingEvidenceBundle != null) {
            supportingEvidenceBundle.stream()
                .map(Element::getValue)
                .map(SupportingEvidenceBundle::getDocument)
                .forEach(documentReference -> {
                    stringBuilder.append(String.format("%s", documentReference.getFilename())).append("\n");
                });
        }

        return stringBuilder.toString().trim();
    }

    @JsonIgnore
    private List<Element<DocumentReference>> getSupportingEvidenceBundleReferences() {
        List<Element<DocumentReference>> documentReferences = new ArrayList<>();

        if (supportingEvidenceBundle != null) {
            supportingEvidenceBundle.stream()
                .map(Element::getValue)
                .map(SupportingEvidenceBundle::getDocument)
                .forEach(documentReference -> {
                    documentReferences.add(element(documentReference));
                });
        }

        return documentReferences;
    }
}
