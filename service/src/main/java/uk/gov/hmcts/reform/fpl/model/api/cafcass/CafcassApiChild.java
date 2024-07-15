package uk.gov.hmcts.reform.fpl.model.api.cafcass;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class CafcassApiChild {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String genderIdentification;
    private String livingSituation;
    private String livingSituationDetails;
    private CafcassApiAddress address;
    private String careAndContactPlan;
    private boolean detailsHidden;
    private String socialWorkerName;
    private String socialWorkerTelephoneNumber;
    private boolean additionalNeeds;
    private String additionalNeedsDetails;
    private String litigationIssues;
    private String litigationIssuesDetails;
    private CafcassApiSolicitor solicitor;
    private String fathersResponsibility;
}
