package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisChild {
    private final String name;
    private final String age;
    private final String gender;
    private final String dateOfBirth;
    private final String address;
    private final String livingSituation;
    private final String keyDates;
    private final String careAndContactPlan;
    private final String adoption;
    private final String placementOrderApplication;
    private final String placementCourt;
    private final String mothersName;
    private final String fathersName;
    /**
     * This historical field is deprecated since DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @Deprecated(since = "DFPL-2362 06/03/25", forRemoval = true)
    private final String fathersResponsibility;
    private final String socialWorkerName;
    private final String socialWorkerTelephoneNumber;
    private final String socialWorkerEmailAddress;
    private final String socialWorkerDetailsHiddenReason;
    private final String additionalNeeds;
    /**
     * This historical field is deprecated since DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @Deprecated(since = "DFPL-2362 06/03/25", forRemoval = true)
    private final String litigationIssues;
    /**
     * This historical field is deprecated since DFPL-2362.
     * @deprecated (DFPL-2362, historical field)
     */
    @Deprecated(since = "DFPL-2362 06/03/25", forRemoval = true)
    private final String detailsHiddenReason;
    private final DocmosisBirthCertificate birthCertificate;
    private final String placementOrderOtherDetails;
}
