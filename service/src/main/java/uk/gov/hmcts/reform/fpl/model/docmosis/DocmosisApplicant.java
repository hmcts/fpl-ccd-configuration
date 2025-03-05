package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisApplicant {
    private final String organisationName;
    private final String contactName;
    private final String address;
    private final String email;
    private final String mobileNumber;
    private final String telephoneNumber;
    private final String pbaNumber;
    private final String solicitorName;
    private final String solicitorMobile;
    private final String solicitorTelephone;
    private final String solicitorEmail;
    private final String solicitorDx;
    private final String solicitorReference;
    private final String representingName;
}
