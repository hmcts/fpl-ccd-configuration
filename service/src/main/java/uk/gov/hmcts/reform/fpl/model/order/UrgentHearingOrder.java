package uk.gov.hmcts.reform.fpl.model.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.ModifiedOrderType;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class UrgentHearingOrder implements AmendableOrder, TranslatableItem {
    public static final UUID COLLECTION_ID = UUID.fromString("5d05d011-5d01-5d01-5d01-5d05d05d05d0");

    @CCD(label = "Order", categoryID = "orders", typeOverride = FieldType.Document)
    DocumentReference order;
    @CCD(label = "Translated document", categoryID = "orders", typeOverride = FieldType.Document)
    DocumentReference translatedOrder;
    @CCD(label = "Unsealed order", showCondition = "order=\"DO_NOT_SHOW\"", typeOverride = FieldType.Document)
    DocumentReference unsealedOrder;
    @CCD(
            label = "Allocation decision",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "AllocationProposalList"
    )
    String allocation;
    @CCD(label = "Date added")
    LocalDate dateAdded;
    @CCD(label = "Amended")
    LocalDate amendedDate;
    @CCD(
            label = " ",
            showCondition = "dateAdded = \"DO NOT SHOW\"",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Others"
    )
    List<Element<Other>> others;
    @CCD(label = "Welsh translation upload time", showCondition = "translatedOrder=\"DO_NOT_SHOW\"")
    LocalDateTime translationUploadDateTime;
    @CCD(label = " ", showCondition = "translationRequirements=\"DO_NOT_SHOW\"", typeOverride = FieldType.Text)
    LanguageTranslationRequirement translationRequirements;

    @Override
    public String asLabel() {
        return "Urgent hearing order - " + (dateAdded == null ? "N/A" : formatLocalDateToString(dateAdded, DATE));
    }

    @Override
    @JsonIgnore
    public boolean hasBeenTranslated() {
        return Objects.nonNull(translatedOrder);
    }

    @Override
    public LocalDateTime translationUploadDateTime() {
        return translationUploadDateTime;
    }

    @Override
    @JsonIgnore
    public DocumentReference getTranslatedDocument() {
        return translatedOrder;
    }

    @Override
    public LanguageTranslationRequirement getTranslationRequirements() {
        return defaultIfNull(translationRequirements, NO);
    }

    @Override
    public LocalDate amendableSortDate() {
        return dateAdded;
    }

    @Override
    @JsonIgnore
    public DocumentReference getDocument() {
        return order;
    }

    @JsonIgnore
    @Override
    public String getModifiedItemType() {
        return ModifiedOrderType.URGENT_HEARING_ORDER.getLabel();
    }

    @JsonIgnore
    @Override
    public List<Element<Other>> getSelectedOthers() {
        return defaultIfNull(this.getOthers(), new ArrayList<>());
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", showCondition = "translationRequirements=\"DO_NOT_SHOW\"")
  private uk.gov.hmcts.reform.fpl.enums.YesNo needTranslation;
  @CCD(
          label = "Sent for translation",
          showCondition = "needTranslation=\"YES\" AND translatedOrder!=\"*\"",
          typeOverride = FieldType.Label
  )
  private String sentForTranslationLabel;
  // ==== end synthesised definition-only fields ====
}
