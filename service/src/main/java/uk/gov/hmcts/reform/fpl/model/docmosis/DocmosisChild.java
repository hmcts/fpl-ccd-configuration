package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings({"java:S1133"})
public class DocmosisChild {
    private final String name;
    private final String age;
    private final String gender;
    private final String dateOfBirth;
    private final String address;
    private final String livingSituation;
    private final String keyDatesTemplate;
    private final String careAndContactPlanTemplate;
    private final String adoptionTemplate;
    private final String placementOrderApplicationTemplate;
    private final String placementCourtTemplate;
    private final String mothersName;
    private final String fathersName;
    /**
     * Deprecated from C110a flow for child section DFPL-2362.
     * @deprecated (DFPL-2362, deprecated field)
     */
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String fathersResponsibilityTemplate;
    private final String socialWorkerName;
    private final String socialWorkerTelephoneNumber;
    private final String socialWorkerEmailAddress;
    private final String socialWorkerDetailsHiddenReason;
    private final String additionalNeeds;
    /**
     * No longer required as part of C110a changes DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String litigationIssuesTemplate;
    /**
     * Replaced by isAddressConfidential and socialWorkerDetailHidden DFPL-2362.
     * @deprecated (DFPL-2362, replaced field)
     */
    @Deprecated(since = "DFPL-2362 06/03/25")
    private final String detailsHiddenReasonTemplate;
    private final DocmosisBirthCertificate birthCertificate;
    private final String placementOrderOtherDetails;
}
