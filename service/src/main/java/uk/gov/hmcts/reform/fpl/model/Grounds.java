package uk.gov.hmcts.reform.fpl.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Grounds {
    @CCD(
            label = "What is the reason behind the child suffering or being likely to suffer significant harm?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "GroundsList",
            typeParameterClass = GroundsList.class
    )
    @NotNull(message = "Select at least one option for how this case meets the threshold criteria")
    @Size(min = 1, message = "Select at least one option for how this case meets the threshold criteria")
    private final List<@NotBlank(message = "Select at least one option for how this case meets the threshold criteria")
        String> thresholdReason;
    @CCD(
            label = "Do you have the threshold document?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "ThresholdDocumentList",
            typeParameterClass = ThresholdDocumentList.class
    )
    private final String hasThresholdDocument;
    @CCD(
            label = "Provide a summary of how this case meets threshold criteria",
            showCondition = "hasThresholdDocument = \"NO\"",
            typeOverride = FieldType.TextArea
    )
    private final String thresholdDetails;
}
