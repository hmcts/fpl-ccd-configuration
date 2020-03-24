package uk.gov.hmcts.reform.fpl.events;

import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;

@Value
public class UpcomingHearingsFound {
    private LocalDate hearingDate;
    private List<CaseDetails> caseDetails;
}
