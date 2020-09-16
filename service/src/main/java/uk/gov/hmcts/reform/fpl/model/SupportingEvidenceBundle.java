package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;
import javax.validation.constraints.PastOrPresent;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupportingEvidenceBundle {
    private final String name;
    private final String notes;
    @PastOrPresent(message = "Date of time received cannot be in the future")
    private final LocalDateTime dateTimeReceived;
    private LocalDateTime dateTimeUploaded;
    private final DocumentReference document;
    private String uploadedBy;
}
