package uk.gov.hmcts.reform.rd.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organisation {
    private String companyNumber;
    private String companyUrl;
    private List<ContactInformation> contactInformation;
    private String name;
    private String organisationIdentifier;
    private List<String> paymentAccount;
    private String sraId;
    private boolean sraRegulated;
    private String status;
    private SuperUser superUser;
}
