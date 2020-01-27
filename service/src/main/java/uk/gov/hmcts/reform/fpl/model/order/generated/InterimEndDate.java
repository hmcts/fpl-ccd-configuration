package uk.gov.hmcts.reform.fpl.model.order.generated;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import javax.validation.constraints.Future;

@Data
@Builder
public class InterimEndDate {
    private final InterimEndDateType type;
    @Future(message = "Enter an end date in the future")
    private final LocalDate endDate;

    public Optional<LocalDateTime> toLocalDateTime() {
        return Optional.ofNullable(endDate).map(date -> LocalDateTime.of(date, LocalTime.of(23, 59, 59)));
    }
}
