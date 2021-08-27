package uk.gov.hmcts.reform.fpl.model.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.ModifiedOrderType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class C110A implements TranslatableItem {

    public static final UUID COLLECTION_ID = UUID.fromString("6d05d011-5d01-5d01-5d01-5d05d05d06d0");

    Language languageRequirementApplication;
    String languageRequirementApplicationNeedWelsh;
    String languageRequirementApplicationNeedEnglish;
    DocumentReference submittedForm;
    DocumentReference translatedSubmittedForm;
    LocalDateTime submittedFormTranslationUploadDateTime;
    LanguageTranslationRequirement submittedFormTranslationRequirements;

    @Override
    @JsonIgnore
    public String asLabel() {
        return "Application (C110A)";
    }

    @Override
    @JsonIgnore
    public String getModifiedItemType() {
        return ModifiedOrderType.C11A.getLabel();
    }

    @Override
    @JsonIgnore
    public List<Element<Other>> getSelectedOthers() {
        return new ArrayList<>();
    }

    @Override
    @JsonIgnore
    public boolean hasBeenTranslated() {
        return Objects.nonNull(translatedSubmittedForm);
    }

    @Override
    @JsonIgnore
    public LocalDateTime translationUploadDateTime() {
        return submittedFormTranslationUploadDateTime;
    }

    @Override
    @JsonIgnore
    public DocumentReference getTranslatedDocument() {
        return translatedSubmittedForm;
    }

    @Override
    @JsonIgnore
    public DocumentReference getDocument() {
        return submittedForm;
    }

    @Override
    @JsonIgnore
    public LanguageTranslationRequirement getTranslationRequirements() {
        return defaultIfNull(submittedFormTranslationRequirements, NO);
    }

    @Override
    @JsonIgnore
    public YesNo getNeedTranslation() {
        return TranslatableItem.super.getNeedTranslation();
    }

    public YesNo getSubmittedFormNeedTranslation() {
        return getNeedTranslation();
    }
}
