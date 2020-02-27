package uk.gov.hmcts.reform.rd.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organisation {
    private String companyNumber;
    private String companyUrl;
    private ArrayList<ContactInformation> contactInformation;
    private String name;
    private String organisationIdentifier;
    private ArrayList<PaymentAccount> paymentAccount;
    private String sraId;
    private boolean sraRegulated;
    private String status;
    private SuperUser superUser;
}
