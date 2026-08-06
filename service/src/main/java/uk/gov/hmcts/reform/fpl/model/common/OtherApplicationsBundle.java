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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.DocumentAcknowledge;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OtherApplicationsBundle implements ApplicationsBundle {
    @CCD(label = " ", showCondition = "applicationType=\"DO NOT SHOW\"", typeOverride = FieldType.Text)
    private final UUID id;
    @CCD(
            label = "Application type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "OtherApplicationType"
    )
    private final OtherApplicationType applicationType;
    @CCD(label = "Please state how soon you want the judge to consider your application?", searchable = false)
    private final UrgencyTimeFrameType urgencyTimeFrameType;
    @CCD(label = "Who's seeking parental responsibility?", searchable = false)
    private final ParentalResponsibilityType parentalResponsibilityType;
    @CCD(
            label = "File",
            regex = ".doc,.docx,.pdf",
            categoryID = "c1AndOtherApplications",
            searchable = false,
            typeOverride = FieldType.Document
    )
    private final DocumentReference document;
    @CCD(label = "Date and time of upload", searchable = false)
    private final String uploadedDateTime;
    @CCD(label = "Uploaded by", searchable = false)
    private final String author;
    @CCD(
            label = "Supporting documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "C1SupportingEvidenceBundle"
    )
    private List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;
    @CCD(
            label = "Supplements",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "C1Supplement"
    )
    private final List<Element<Supplement>> supplementsBundle;
    @CCD(label = "Applicant", searchable = false)
    private final String applicantName;
    @CCD(
            label = " ",
            showCondition = "applicationType=\"DO NOT SHOW\"",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentNew"
    )
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

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "People notified")
  private String othersNotified;
  @CCD(label = " ", showCondition = "applicationType=\"DO NOT SHOW\"")
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Others>> others;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabel;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabelForCYA;
  @CCD(label = "Tick to confirm this document is related to this case", searchable = false)
  private java.util.Set<DocumentAcknowledge> documentAcknowledge;
  // ==== end synthesised definition-only fields ====
}
