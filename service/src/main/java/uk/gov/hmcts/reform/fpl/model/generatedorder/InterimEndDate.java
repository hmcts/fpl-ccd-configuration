package uk.gov.hmcts.reform.fpl.model.generatedorder;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.validation.constraints.Future;

@Data
@Builder
public class InterimEndDate {
    private final InterimEndDateType endDateType;
    @Future
    private final LocalDate endDate;

    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.of(endDate, LocalTime.of(23,59,59));
    }
}
