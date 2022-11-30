package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType;
import uk.gov.hmcts.reform.fpl.enums.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.interfaces.ApplicationsBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OtherApplicationsBundle implements ApplicationsBundle {
    private final UUID id;
    private final OtherApplicationType applicationType;
    private final UrgencyTimeFrameType urgencyTimeFrameType;
    private final ParentalResponsibilityType parentalResponsibilityType;
    private final DocumentReference document;
    private final String uploadedDateTime;
    private final String author;
    private List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;
    private final List<Element<Supplement>> supplementsBundle;
    private final String applicantName;
    private final List<Element<Respondent>> respondents;

    public String toLabel() {
        return format("%s, %s",
            StringUtils.substringBefore(applicationType.getLabel(), " "), uploadedDateTime);
    }

    @Override
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle() {
        return defaultIfNull(supportingEvidenceBundle, new ArrayList<>());
    }

    public List<Element<Supplement>> getSupplementsBundle() {
        return defaultIfNull(supplementsBundle, new ArrayList<>());
    }

    @Override
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceLA() {
        return getSupportingEvidenceBundle().stream()
            .filter(doc -> !(doc.getValue().isUploadedByHMCTS() && doc.getValue().isConfidentialDocument()))
            .collect(Collectors.toList());
    }

    @Override
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceNC() {
        return getSupportingEvidenceBundle().stream()
            .filter(doc -> !doc.getValue().isConfidentialDocument())
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public String getAllDocumentFileNames() {
        String fileName = "";

        if (document != null) {
            fileName = document.getFilename();
        }

        String stringBuilder = fileName + "\n" + getSupportingEvidenceFileNames();
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
    @Override
    public int getSortOrder() {
        return applicationType.getSortOrder();
    }

    @JsonIgnore
    @Override
    public DocumentReference getApplication() {
        return document;
    }
}
