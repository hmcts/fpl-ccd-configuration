package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.validation.groups.SecureAccommodationGroup;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class GroundsForSecureAccommodationOrder {
    @NotNull(message = "Select at least one option for how this case meets grounds for a secure accommodation order",
        groups = SecureAccommodationGroup.class)
    @Size(min = 1,
        message = "Select at least one option for how this case meets grounds for an secure accommodation order",
        groups = SecureAccommodationGroup.class)
    private List<@NotBlank(
        message = "Select at least one option for how this case meets grounds for an secure accommodation order",
        groups = SecureAccommodationGroup.class) String> grounds;

    @NotBlank(message = "Please give reasons for the application and length of the order sought",
        groups = SecureAccommodationGroup.class)
    private String reasonAndLength;

    private List<DocumentReference> supportingDocuments;
}
