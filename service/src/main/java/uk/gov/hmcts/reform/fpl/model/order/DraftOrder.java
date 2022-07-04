package uk.gov.hmcts.reform.fpl.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class DraftOrder {
    private String title;
    private DocumentReference document;
    private LocalDate dateUploaded;
}
