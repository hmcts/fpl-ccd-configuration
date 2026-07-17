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
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder
public class InterimEndDate {
    @CCD(label = " ")
    private final InterimEndDateType type;
    @CCD(label = " ", showCondition = "type = \"NAMED_DATE\"")
    @Future(message = "Enter an end date in the future", groups = InterimEndDateGroup.class)
    private final LocalDate endDate;
    @CCD(
            label = " ",
            hint = "Use 24 hour clock, for example 15 30 00",
            showCondition = "type = \"SPECIFIC_TIME_NAMED_DATE\""
    )
    @Future(message = "Enter an end date in the future", groups = InterimEndDateGroup.class)
    private final LocalDateTime endDateTime;

    public Optional<LocalDateTime> toLocalDateTime() {
        return Optional.ofNullable(endDate).map(date -> LocalDateTime.of(date, LocalTime.of(23, 59, 59)));
    }
}
