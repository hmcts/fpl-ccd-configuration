package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.json.deserializer.DynamicListDeserializer;
import uk.gov.hmcts.reform.fpl.json.deserializer.YesNoDeserializer;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawJudiciaryCaseworkerPubliclawMagistrateCrudAccess;

@Value
@Builder
@Jacksonized
public class ConfirmApplicationReviewedEventData {
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawJudiciaryCaseworkerPubliclawMagistrateCrudAccess.class}
    )
    @JsonDeserialize(using = YesNoDeserializer.class)
    YesNo hasApplicationToBeReviewed;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawJudiciaryCaseworkerPubliclawMagistrateCrudAccess.class}
    )
    @JsonDeserialize(using = YesNoDeserializer.class)
    YesNo onlyOneApplicationToBeReviewed;

    @CCD(
            label = "Select one of the additional application bundles to review",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawJudiciaryCaseworkerPubliclawMagistrateCrudAccess.class}
    )
    @JsonDeserialize(using = DynamicListDeserializer.class)
    DynamicList additionalApplicationToBeReviewedList;

    @CCD(
            label = "Additional Application Bundle",
            searchable = false,
            access = {CaseworkerPubliclawJudiciaryCaseworkerPubliclawMagistrateCrudAccess.class}
    )
    AdditionalApplicationsBundle additionalApplicationsBundleToBeReviewed;
    @CCD(
            label = "Additional Application Bundle",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ConfirmApplicationReviewedCheckBox",
            access = {CaseworkerPubliclawJudiciaryCaseworkerPubliclawMagistrateCrudAccess.class}
    )
    List<String> confirmApplicationReviewed;

    public static List<String> eventFields() {
        return List.of("hasApplicationToBeReviewed",
            "onlyOneApplicationToBeReviewed",
            "additionalApplicationToBeReviewedList",
            "additionalApplicationsBundleToBeReviewed",
            "confirmApplicationReviewed");
    }
}
