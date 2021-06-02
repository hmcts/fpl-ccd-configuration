package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class DocmosisStandardDirectionOrder extends DocmosisOrder {
    private final String complianceDeadline;
    private final List<DocmosisRespondent> respondents;
    private final boolean respondentsProvided;
    private final String applicantName;
    private final DocmosisHearingBooking hearingBooking;
}
