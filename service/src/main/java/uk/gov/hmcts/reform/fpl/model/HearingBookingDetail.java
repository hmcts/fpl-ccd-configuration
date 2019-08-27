package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.interfaces.HearingBookingDetailsGroup;
import uk.gov.hmcts.reform.validators.interfaces.HasValidDate;

import java.time.LocalDate;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
@HasValidDate
public class HearingBookingDetail {
    @NotBlank(message = "Select the type of hearing", groups = HearingBookingDetailsGroup.class)
    private final String hearingType;
    private final String hearingTypeDetails;
    @NotBlank(message = "Enter the venue", groups = HearingBookingDetailsGroup.class)
    private final String venue;
    private final LocalDate hearingDate;
    @NotBlank(message = "Enter the pre hearing attendance time", groups = HearingBookingDetailsGroup.class)
    private final String preHearingAttendance;
    @NotBlank(message = "Enter the hearing time", groups = HearingBookingDetailsGroup.class)
    private final String hearingTime;
    @NotEmpty(message = "Enter the hearing needs booked", groups = HearingBookingDetailsGroup.class)
    private final List<String> hearingNeededDetails;
    private final String hearingNeededGiveDetails;
    @NotBlank(message = "Enter the judge or magistrate's title", groups = HearingBookingDetailsGroup.class)
    private final String judgeTitle;
    @NotBlank(message = "Enter the judge or magistrate's full name", groups = HearingBookingDetailsGroup.class)
    private final String judgeFullName;
}
