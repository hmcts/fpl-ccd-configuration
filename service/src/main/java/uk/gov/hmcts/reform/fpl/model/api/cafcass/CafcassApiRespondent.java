package uk.gov.hmcts.reform.fpl.model.api.cafcass;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CafcassApiRespondent {
    private String firstName;
    private String lastName;
    private String gender;
    private String genderIdentification;
    private boolean addressKnown;
    private String addressUnknownReason;
    private CafcassApiAddress address;
    private LocalDate dateOfBirth;
    private String telephoneNumber;
    private String litigationIssues;
    private String litigationIssuesDetails;
    private String contactDetailsHidden;
    private String contactDetailsHiddenReason;
    private String relationshipToChild;
    private CafcassApiSolicitor solicitor;
}
