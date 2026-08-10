package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentAction;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentRemovalReason;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACruPlus28RolesNrpimkAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CAFCASSSOLICITORCruPlus4RolesConemxAccess;
import uk.gov.hmcts.reform.fpl.model.DocumentAcknowledge;

@Value
@Jacksonized
@Builder
@JsonInclude(value = NON_NULL)
public class ManageDocumentEventData {
    @CCD(
            label = " ",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCruPlus4RolesConemxAccess.class}
    )
    @Temp
    ManageDocumentAction manageDocumentAction;
    @CCD(
            label = " ",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCruPlus4RolesConemxAccess.class}
    )
    @Temp
    ManageDocumentRemovalReason manageDocumentRemoveDocReason;
    @CCD(
            label = " ",
            hint = "Tell us why you are removing this document",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCruPlus4RolesConemxAccess.class}
    )
    @Temp
    String manageDocumentRemoveDocAnotherReason;
    @CCD(
            label = "Add a document",
            hint = "Upload a document to the system",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCruPlus4RolesConemxAccess.class}
    )
    @Temp
    List<Element<UploadableDocumentBundle>> uploadableDocumentBundle;
    @CCD(
            label = "Has confidential party?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCruPlus4RolesConemxAccess.class}
    )
    @Temp
    String hasConfidentialParty;
    @CCD(
            label = "Show placement notice recipient type?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCruPlus4RolesConemxAccess.class}
    )
    @Temp
    String askForPlacementNoticeRecipientType;
    @CCD(
            label = "All documents are related to ${caseName}",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DocumentAcknowledge",
            typeParameterClass = DocumentAcknowledge.class,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCruPlus4RolesConemxAccess.class}
    )
    @Temp
    List<String> documentAcknowledge;
    @CCD(
            label = "Allow marking document confidential",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCruPlus4RolesConemxAccess.class}
    )
    @Temp
    String allowMarkDocumentConfidential;
    @CCD(
            label = "Allow select document type to remove document",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCruPlus4RolesConemxAccess.class}
    )
    @Temp
    String allowSelectDocumentTypeToRemoveDocument;
    @CCD(
            label = "Document type",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCruPlus4RolesConemxAccess.class}
    )
    @Temp
    private DynamicList availableDocumentTypesForRemoval;
    @CCD(
            label = "Uploaded Document",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCruPlus4RolesConemxAccess.class}
    )
    @Temp
    private DynamicList documentsToBeRemoved;

    public static List<String> temporaryFields() {
        List<String> tempFields = getFieldsListWithAnnotation(ManageDocumentEventData.class, Temp.class).stream()
            .map(Field::getName)
            .toList();
        return tempFields;
    }

    public List<Element<UploadableDocumentBundle>> getUploadableDocumentBundle() {
        return defaultIfNull(this.uploadableDocumentBundle, new ArrayList<>());
    }

    public DocumentType getSelectedDocumentTypeToRemove() {
        if (getAvailableDocumentTypesForRemoval() != null && getAvailableDocumentTypesForRemoval().getValue() != null
            && !StringUtils.isEmpty(getAvailableDocumentTypesForRemoval().getValue().getCode())) {
            return DocumentType.valueOf(getAvailableDocumentTypesForRemoval().getValue().getCode());
        }
        return null;
    }

}
