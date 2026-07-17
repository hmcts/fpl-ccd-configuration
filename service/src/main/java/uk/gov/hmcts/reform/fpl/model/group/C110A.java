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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACruPlus28RolesNrpimkAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERRPlus3RolesDckcthAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcasssystemupdateRPlus1RolesQakbhsAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcasssystemupdateRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERRPlus37RolesQutwpjAccess;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class C110A implements TranslatableItem {

    public static final UUID COLLECTION_ID = UUID.fromString("6d05d011-5d01-5d01-5d01-5d05d05d06d0");

    @CCD(
            label = "Which language are you using to complete this application?",
            searchable = false,
            access = {DefaultAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class}
    )
    Language languageRequirementApplication;
    @CCD(
            label = "Does this application need to be translated into Welsh?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class}
    )
    String languageRequirementApplicationNeedWelsh;
    @CCD(
            label = "Does this application need to be translated into English?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class}
    )
    String languageRequirementApplicationNeedEnglish;
    @CCD(
            label = "Application Form",
            categoryID = "originalApplications",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, BARRISTERRPlus3RolesDckcthAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesQakbhsAccess.class, LABARRISTERRAccess.class}
    )
    DocumentReference submittedForm;
    @CCD(
            label = "Translated document",
            categoryID = "originalApplications",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERRPlus3RolesDckcthAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class, LABARRISTERCruAccess.class, CaseworkerPubliclawCafcasssystemupdateRAccess.class}
    )
    DocumentReference translatedSubmittedForm;
    @CCD(
            label = "Application Supplement",
            categoryID = "originalApplications",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {BARRISTERRPlus37RolesQutwpjAccess.class}
    )
    DocumentReference supplementDocument;
    @CCD(
            label = "Welsh translation upload time",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERRPlus3RolesDckcthAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class, LABARRISTERCruAccess.class}
    )
    LocalDateTime submittedFormTranslationUploadDateTime;
    @CCD(
            label = "Welsh translation requirements",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERRPlus3RolesDckcthAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class, LABARRISTERRAccess.class}
    )
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
