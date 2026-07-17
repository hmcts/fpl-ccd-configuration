package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class UploadTranslationsEventData {

    @CCD(
            label = "Choose the document that's been translated",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class}
    )
    DynamicList uploadTranslationsRelatedToDocument;
    @CCD(
            label = "Original document",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class}
    )
    DocumentReference uploadTranslationsOriginalDoc;
    @CCD(
            label = "Upload translated document",
            hint = "Orders will be sealed before sending to relevant parties",
            regex = ".doc,.docx,.pdf",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class}
    )
    DocumentReference uploadTranslationsTranslatedDoc;

    @JsonIgnore
    public List<String> getTransientFields() {
        return List.of(
            "uploadTranslationsRelatedToDocument",
            "uploadTranslationsOriginalDoc",
            "uploadTranslationsTranslatedDoc"
        );
    }

}
