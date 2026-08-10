package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.ModifiedOrderType;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "NoticeOfProceedingsBundle", generate = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class DocumentBundle implements TranslatableItem {
    @CCD(label = "File name", categoryID = "hearingNotices", typeOverride = FieldType.Document)
    private final DocumentReference document;
    @CCD(label = "Translated document", categoryID = "hearingNotices", typeOverride = FieldType.Document)
    private final DocumentReference translatedDocument;
    @CCD(label = "Welsh translation upload time", showCondition = "translatedDocument=\"DO_NOT_SHOW\"")
    private final LocalDateTime translationUploadDateTime;
    @CCD(label = " ", showCondition = "translationRequirements=\"DO_NOT_SHOW\"", typeOverride = FieldType.Text)
    private final LanguageTranslationRequirement translationRequirements;

    @Override
    @JsonIgnore
    public String asLabel() {
        return Optional.ofNullable(document)
            .map(documentReference ->
                "Notice of proceedings (" + (documentReference.getFilename().contains("_c6a") ? "C6A" : "C6") + ")")
            .orElse("");
    }

    @Override
    @JsonIgnore
    public boolean hasBeenTranslated() {
        return Objects.nonNull(translatedDocument);
    }

    @Override
    @JsonIgnore
    public String getModifiedItemType() {
        return ModifiedOrderType.NOTICE_OF_PROCEEDINGS.getLabel();
    }

    @Override
    public LocalDateTime translationUploadDateTime() {
        return translationUploadDateTime;
    }

    @Override
    public LanguageTranslationRequirement getTranslationRequirements() {
        return defaultIfNull(translationRequirements, NO);
    }

    @Override
    @JsonIgnore
    public List<Element<Other>> getSelectedOthers() {
        return new ArrayList<>();
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Sent for translation",
          showCondition = "needTranslation=\"YES\" AND translatedDocument!=\"*\"",
          typeOverride = FieldType.Label
  )
  private String sentForTranslationLabel;
  // ==== end synthesised definition-only fields ====
}
