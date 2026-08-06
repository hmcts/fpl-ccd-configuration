package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.ModifiedOrderType;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.IssuableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateFromStringUsingFormat;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Slf4j
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardDirectionOrder implements IssuableOrder, RemovableOrder, AmendableOrder, TranslatableItem {
    public static final UUID COLLECTION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID UDO_COLLECTION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @CCD(label = "The date of the hearing", typeOverride = FieldType.Date)
    private final String hearingDate;
    @CCD(label = "Date of issue")
    private final String dateOfIssue;
    @CCD(label = "Do you want to send the order now?", showCondition = "directions=\"DO NOT SHOW\"")
    private final OrderStatus orderStatus;
    @CCD(label = "Judge and Justices' Legal Adviser")
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    @CCD(label = "Date uploaded")
    private final LocalDate dateOfUpload;
    @CCD(label = "Uploaded by")
    private final String uploader;
    @CCD(label = "Amended")
    private final LocalDate amendedDate;
    @CCD(
            label = "Unsealed copy",
            showCondition = "orderStatus=\"DO_NOT_SHOW\"",
            categoryID = "orders",
            typeOverride = FieldType.Document
    )
    private final DocumentReference unsealedDocumentCopy;
    @CCD(label = "Custom directions", showCondition = "orderStatus=\"DO_NOT_SHOW\"")
    private final List<Element<CustomDirection>> customDirections;
    @CCD(label = "Standard directions", showCondition = "orderStatus=\"DO_NOT_SHOW\"")
    private final List<Element<StandardDirection>> standardDirections;
    @CCD(label = "Directions", showCondition = "orderStatus=\"DO_NOT_SHOW\"")
    private List<Element<Direction>> directions;
    @CCD(label = "File", categoryID = "orders", typeOverride = FieldType.Document)
    private DocumentReference orderDoc;
    @CCD(label = "Translated document", categoryID = "orders", typeOverride = FieldType.Document)
    private DocumentReference translatedOrderDoc;
    @CCD(
            label = "File",
            showCondition = "orderStatus=\"DO_NOT_SHOW\"",
            categoryID = "orders",
            typeOverride = FieldType.Document
    )
    private DocumentReference lastUploadedOrder;
    @CCD(label = "Reason for removal", typeOverride = FieldType.TextArea)
    private String removalReason;
    @CCD(
            label = " ",
            showCondition = "standardDirections = \"DO NOT SHOW\"",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Others"
    )
    private final List<Element<Other>> others;
    @CCD(label = "Welsh translation upload time", showCondition = "translatedOrderDoc=\"DO_NOT_SHOW\"")
    private final LocalDateTime translationUploadDateTime;
    @CCD(label = " ", showCondition = "translationRequirements=\"DO_NOT_SHOW\"", typeOverride = FieldType.Text)
    private final LanguageTranslationRequirement translationRequirements;

    @JsonIgnore
    @Setter
    private Boolean orderTypeIsSdo; // for removal tools use only

    @JsonIgnore
    public boolean isSealed() {
        return SEALED == orderStatus;
    }

    @JsonIgnore
    public void setDirectionsToEmptyList() {
        this.directions = emptyList();
    }

    @JsonIgnore
    public void setOrderDocReferenceFromDocument(Document document) {
        if (document != null) {
            this.orderDoc = buildFromDocument(document);
        }
    }

    @JsonIgnore
    public UUID getCollectionId() {
        if (!Boolean.FALSE.equals(orderTypeIsSdo)) {
            return COLLECTION_ID;
        } else {
            return UDO_COLLECTION_ID;
        }
    }

    @Override
    @JsonIgnore
    public boolean isRemovable() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean hasBeenTranslated() {
        return Objects.nonNull(translatedOrderDoc);
    }

    @Override
    public LocalDateTime translationUploadDateTime() {
        return translationUploadDateTime;
    }

    @Override
    public String asLabel() {
        String formattedDate = Optional.ofNullable(dateOfIssue)
            .orElse(formatLocalDateToString(defaultIfNull(dateOfUpload, LocalDate.now()), DATE));

        return ((!Boolean.FALSE.equals(orderTypeIsSdo))
            ? "Gatekeeping order - " : "Urgent directions order - ") + formattedDate;
    }

    @Override
    public LocalDate amendableSortDate() {
        if (null != dateOfUpload) {
            return dateOfUpload;
        }

        try {
            if (null != dateOfIssue) {
                return parseLocalDateFromStringUsingFormat(dateOfIssue, DATE);
            }
        } catch (DateTimeParseException ignored) {
            log.warn("Could not parse {} with format {}", dateOfIssue, DATE);
        }

        log.warn("Could not find any date to sort amendable list by, falling back to null");
        return null;
    }

    @Override
    @JsonIgnore
    public DocumentReference getTranslatedDocument() {
        return translatedOrderDoc;
    }

    @Override
    public LanguageTranslationRequirement getTranslationRequirements() {
        return defaultIfNull(translationRequirements, NO);
    }

    @Override
    @JsonIgnore
    public DocumentReference getDocument() {
        return orderDoc;
    }

    @JsonIgnore
    @Override
    public String getModifiedItemType() {
        return ModifiedOrderType.STANDARD_DIRECTION_ORDER.getLabel();
    }

    @JsonIgnore
    @Override
    public List<Element<Other>> getSelectedOthers() {
        return defaultIfNull(this.getOthers(), new ArrayList<>());
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Sent for translation",
          showCondition = "needTranslation=\"YES\" AND translatedOrderDoc!=\"*\" AND orderStatus=\"SEALED\"",
          typeOverride = FieldType.Label
  )
  private String sentForTranslationLabel;
  // ==== end synthesised definition-only fields ====
}

