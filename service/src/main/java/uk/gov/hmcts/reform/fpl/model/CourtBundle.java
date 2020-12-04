package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor()
public class CourtBundle {
    private String hearing;
    private DocumentReference document;
    private LocalDateTime dateTimeUploaded;
    private String uploadedBy;
}
