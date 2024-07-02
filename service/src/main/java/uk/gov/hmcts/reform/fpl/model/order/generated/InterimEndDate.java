package uk.gov.hmcts.reform.fpl.model.order.generated;

import jakarta.validation.constraints.Future;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType;
import uk.gov.hmcts.reform.fpl.validation.groups.InterimEndDateGroup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Data
@Builder
public class InterimEndDate {
    private final InterimEndDateType type;
    @Future(message = "Enter an end date in the future", groups = InterimEndDateGroup.class)
    private final LocalDate endDate;
    @Future(message = "Enter an end date in the future", groups = InterimEndDateGroup.class)
    private final LocalDateTime endDateTime;

    public Optional<LocalDateTime> toLocalDateTime() {
        return Optional.ofNullable(endDate).map(date -> LocalDateTime.of(date, LocalTime.of(23, 59, 59)));
    }
}
