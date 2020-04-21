package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.Address;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisRespondent {
    private final String name;
    private final String age;
    private final String gender;
    private final String dateOfBirth;
    private final String placeOfBirth;
    private final Address address;
    private final String contactDetailsHidden;
    private final String contactDetailsHiddenReason;
    private final String telephoneNumber;
    private final String relationshipToChild;
    private final String litigationIssues;
    private final String litigationIssuesDetails;
}
