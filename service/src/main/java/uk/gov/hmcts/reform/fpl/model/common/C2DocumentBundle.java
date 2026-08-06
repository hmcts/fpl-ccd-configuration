package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType;
import uk.gov.hmcts.reform.fpl.enums.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.interfaces.ApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.order.DraftOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.model.DraftOrderUrgencyOption;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.DocumentAcknowledge;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true, value = {"supportingEvidenceLA", "supportingEvidenceNC"})
public class C2DocumentBundle implements ApplicationsBundle {
    @CCD(label = " ", showCondition = "type=\"DO NOT SHOW\"", typeOverride = FieldType.Text)
    private final UUID id;
    @CCD(label = "Application type")
    private C2ApplicationType type;
    @CCD(label = "Please state how soon you want the judge to consider your application?", searchable = false)
    private final UrgencyTimeFrameType urgencyTimeFrameType;
    @CCD(label = "Name of representative")
    private final String nameOfRepresentative;
    @CCD(label = "Do you want to enter PBA details?", typeOverride = FieldType.YesOrNo)
    private final String usePbaPayment;
    @CCD(label = "Payment by account (PBA) number", hint = "For example, PBA1234567")
    private final String pbaNumber;
    @CCD(label = "Client code")
    private final String clientCode;
    @CCD(label = "Customer reference")
    private final String fileReference;
    @CCD(label = "File", regex = ".doc,.docx,.pdf", categoryID = "c2Applications", typeOverride = FieldType.Document)
    private final DocumentReference document;
    @CCD(label = "Notes", typeOverride = FieldType.TextArea)
    private final String description;
    @CCD(label = "Date and time of upload")
    private final String uploadedDateTime;
    @CCD(label = "Uploaded by")
    private final String author;
    @CCD(
            label = "Supporting documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "C2SupportingEvidenceBundle"
    )
    private List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;
    @CCD(label = "Draft Orders", typeOverride = FieldType.Collection, typeParameterOverride = "C2DraftOrder")
    private List<Element<DraftOrder>> draftOrdersBundle;
    @CCD(label = "Supplements", typeOverride = FieldType.Collection, typeParameterOverride = "C2Supplement")
    private final List<Element<Supplement>> supplementsBundle;
    @CCD(label = "Additional orders requested", hint = "Select all that apply", searchable = false)
    private final List<C2AdditionalOrdersRequested> c2AdditionalOrdersRequested;
    @CCD(label = "Who's seeking parental responsibility?", searchable = false)
    private final ParentalResponsibilityType parentalResponsibilityType;
    @CCD(label = "Applicant", searchable = false)
    private final String applicantName;
    @CCD(
            label = " ",
            showCondition = "type=\"DO NOT SHOW\"",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentNew"
    )
    private final List<Element<Respondent>> respondents;
    @CCD(label = "Requested hearing to adjourn", searchable = false)
    private final String requestedHearingToAdjourn;
    @CCD(label = "Which hearing?", searchable = false, typeOverride = FieldType.DynamicList)
    private final DynamicList hearingList;

    public String toLabel(int index) {
        return format("Application %d: %s", index, uploadedDateTime);
    }

    public String toLabel() {
        return format("C2, %s", uploadedDateTime);
    }

    @JsonIgnore
    @Override
    public int getSortOrder() {
        return 2;
    }

    @JsonIgnore
    @Override
    public DocumentReference getApplication() {
        return document;
    }

    @Override
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle() {
        return defaultIfNull(supportingEvidenceBundle, new ArrayList<>());
    }

    @Deprecated
    @Override
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceLA() {
        return getSupportingEvidenceBundle().stream()
            .filter(doc -> !(doc.getValue().isUploadedByHMCTS() && doc.getValue().isConfidentialDocument()))
            .collect(Collectors.toList());
    }

    @Deprecated
    @Override
    public List<Element<SupportingEvidenceBundle>> getSupportingEvidenceNC() {
        return getSupportingEvidenceBundle().stream()
            .filter(doc -> !doc.getValue().isConfidentialDocument())
            .collect(Collectors.toList());
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

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "People notified")
  private String othersNotified;
  @CCD(label = "Do one or more orders need priority approval from the judge?")
  private DraftOrderUrgencyOption draftOrderUrgency;
  @CCD(label = " ", showCondition = "type=\"DO NOT SHOW\"", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Others>> others;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabel;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabelForCYA;
  @CCD(label = "Tick to confirm this document is related to this case", searchable = false)
  private java.util.Set<DocumentAcknowledge> documentAcknowledge;
  // ==== end synthesised definition-only fields ====
}
