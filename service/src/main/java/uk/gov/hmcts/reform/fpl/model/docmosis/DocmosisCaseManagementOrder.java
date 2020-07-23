package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;

import java.util.List;

/**
 * Docmosis model for the CMO order.
 *
 * @deprecated remove once FPLA-1915 goes live
 */
@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Deprecated(since = "FPLA-1915")
@SuppressWarnings("java:S1133") // Remove once deprecations dealt with
public class DocmosisCaseManagementOrder extends DocmosisOrder {
    private final List<DocmosisRepresentative> representatives;
    private final boolean scheduleProvided;
    private final Schedule schedule;
    private final List<DocmosisRecital> recitals;
    private final boolean recitalsProvided;
    private final DocmosisJudge allocatedJudge;
    private final String complianceDeadline;
    private final List<DocmosisRespondent> respondents;
    private final boolean respondentsProvided;
    private final String applicantName;
    private final DocmosisHearingBooking hearingBooking;
}
