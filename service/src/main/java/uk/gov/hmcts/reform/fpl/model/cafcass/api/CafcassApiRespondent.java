package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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
    private boolean contactDetailsHidden;
    private String contactDetailsHiddenReason;
    private String relationshipToChild;
    private CafcassApiSolicitor solicitor;
}
