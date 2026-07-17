package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.capitalize;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class ReturnApplication {
    @CCD(
            label = "Reason for rejection",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ReturnedReason"
    )
    private final List<ReturnedApplicationReasons> reason;
    @CCD(
            label = "Let the local authority know what they need to change",
            showCondition = "reason!=\"\"",
            typeOverride = FieldType.TextArea
    )
    private final String note;
    @CCD(label = "Date submitted")
    private String submittedDate;
    @CCD(label = "Date returned")
    private String returnedDate;
    @CCD(label = "Document", typeOverride = FieldType.Document)
    private DocumentReference document;

    @JsonIgnore
    public String getFormattedReturnReasons() {
        if (reason != null) {
            String formattedReasons = reason.stream()
                .map(ReturnedApplicationReasons::getLabel)
                .collect(Collectors.joining(", "));

            return capitalize(formattedReasons.toLowerCase());
        }

        return "";
    }
}
