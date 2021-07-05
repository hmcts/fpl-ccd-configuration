package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdditionalApplicationsBundle {
    private final String uploadedDateTime;
    private final String author;
    private C2DocumentBundle c2DocumentBundle;
    private OtherApplicationsBundle otherApplicationsBundle;
    private PBAPayment pbaPayment;

    @JsonIgnore
    public String toLabel() {
        if (isNotEmpty(c2DocumentBundle) && isNotEmpty(otherApplicationsBundle)) {
            return String.format("C2, %s", otherApplicationsBundle.toLabel());
        } else if (isNotEmpty(c2DocumentBundle)) {
            return c2DocumentBundle.toLabel();
        }
        return otherApplicationsBundle.toLabel();

    }
}
