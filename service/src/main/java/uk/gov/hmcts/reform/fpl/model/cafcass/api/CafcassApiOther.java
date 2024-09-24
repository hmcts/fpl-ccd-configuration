package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CafcassApiOther {
    private String name;
    private String dateOfBirth;
    private String gender;
    private String genderIdentification;
    private String birthPlace;
    private boolean addressKnown;
    private String addressUnknownReason;
    private CafcassApiAddress address;
    private String telephone;
    private String litigationIssues;
    private String litigationIssuesDetails;
    private boolean detailsHidden;
    private String detailsHiddenReason;
}
