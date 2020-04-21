package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.Address;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisApplicant {
    private final String organisationName;
    private final String contactDirection;
    private final String jobTitle;
    private final Address address;
    private final String email;
    private final String mobileNumber;
    private final String telephoneNumber;
    private final String pbaNumber;
    private final String solicitorName;
    private final String solicitorMobile;
    private final String solicitorTelephone;
    private final String solicitorEmail;
    private final String solicitorDXNumber;
    private final String solicitorReference;
}
