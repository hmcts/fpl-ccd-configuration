package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisBirthCertificate {

    String number;
    String date;
    String registrationDistrict;
    String registrationSubDistrict;
    String registrationCounty;

}
