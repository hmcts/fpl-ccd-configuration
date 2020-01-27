package uk.gov.hmcts.reform.fpl.model.order.generated;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.validation.constraints.Future;

import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.NAMED_DATE;

@Data
@Builder
public class InterimEndDate {
    private final InterimEndDateType type;
    @Future(message = "Enter an end date in the future")
    private final LocalDate endDate;

    public LocalDateTime toLocalDateTime() {
        return endDate == null ? null : LocalDateTime.of(endDate, LocalTime.of(23,59,59));
    }

    public boolean hasEndDate() {
        return type == NAMED_DATE && endDate != null;
    }
}
