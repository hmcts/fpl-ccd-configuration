package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import javax.validation.Valid;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseData {
    private final List<Element<Applicant>> applicants;

    @Valid
    private final List<Element<HearingBookingDetail>> hearingDetails;
}
