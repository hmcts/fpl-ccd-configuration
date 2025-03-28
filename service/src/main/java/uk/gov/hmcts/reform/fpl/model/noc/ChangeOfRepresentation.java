package uk.gov.hmcts.reform.fpl.model.noc;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
public class ChangeOfRepresentation {
    String respondent;
    String child;
    LocalDate date;
    String by;
    String via;

    ChangedRepresentative removed;
    ChangedRepresentative added;
}
