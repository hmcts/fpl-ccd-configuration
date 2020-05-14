package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisOtherParty {
    private final String name;
    private final String gender;
    private final String dateOfBirth;
    private final String placeOfBirth;
    private final String address;
    private final String detailsHidden;
    private final String detailsHiddenReason;
    private final String telephoneNumber;
    private final String relationshipToChild;
    private final String litigationIssuesDetails;
}
