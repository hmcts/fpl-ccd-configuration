package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationOrderGround;
import uk.gov.hmcts.reform.fpl.validation.groups.SecureAccommodationGroup;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class GroundsForSecureAccommodationOrder {
    @CCD(
            label = " ",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "SecureAccommodationOrderGroundList"
    )
    @NotNull(message = "Select at least one option for how this case meets grounds for a secure accommodation order",
        groups = SecureAccommodationGroup.class)
    @Size(min = 1,
        message = "Select at least one option for how this case meets grounds for an secure accommodation order",
        groups = SecureAccommodationGroup.class)
    private List<SecureAccommodationOrderGround> grounds;

    @CCD(label = "The reason(s) for the application and length of order applied for", typeOverride = FieldType.TextArea)
    @NotBlank(message = "Please give reasons for the application and length of the order sought",
        groups = SecureAccommodationGroup.class)
    private String reasonAndLength;
}
