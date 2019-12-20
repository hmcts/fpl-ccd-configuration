package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Other {
    @SuppressWarnings("membername")
    private final String DOB;
    private final String name;
    private final String gender;
    private final Address address;
    private final String telephone;
    private final String birthplace;
    private final String childInformation;
    private final String detailsHidden;
    private final String litigationIssues;
    private final String genderIdentification;
}
