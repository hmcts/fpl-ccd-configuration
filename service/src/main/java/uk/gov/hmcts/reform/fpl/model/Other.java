package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.interfaces.Representable;

@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class Other extends Representable {
    @SuppressWarnings("membername")
    private final String DOB;
    private final String name;
    private final String gender;
    private final Address address;
    private final String telephone;
    private final String birthplace;
    private final String childInformation;
    private final String litigationIssues;
    private final String genderIdentification;
}
