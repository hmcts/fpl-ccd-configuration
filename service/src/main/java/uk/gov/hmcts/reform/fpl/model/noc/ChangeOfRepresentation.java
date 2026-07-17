package uk.gov.hmcts.reform.fpl.model.noc;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Value
@Builder(toBuilder = true)
public class ChangeOfRepresentation {
    @CCD(label = "Respondent")
    String respondent;
    @CCD(label = "Child")
    String child;
    @CCD(label = "Date")
    LocalDate date;
    @CCD(label = "Updated by")
    String by;
    @CCD(label = "Updated via")
    String via;

    @CCD(label = "Removed representative")
    ChangedRepresentative removed;
    @CCD(label = "Added representative")
    ChangedRepresentative added;
}
