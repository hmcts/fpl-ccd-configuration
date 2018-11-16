package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.regex.Pattern;

@Service
public class UserService {

    private final IdamApi idamApi;

    @Autowired
    public UserService(IdamApi idamApi) {
        this.idamApi = idamApi;
    }

    public String getUserDetails(String authorization) {
        UserDetails userDetails = idamApi.retrieveUserDetails(authorization);
        String email = userDetails.getEmail();
        return getEmailDomain(email);
    }

    private String getEmailDomain(String email) {
        int index = email.indexOf("@");
        String temp = email.substring(index + 1);

        int index2 = temp.indexOf(".");
        String caseLocalAuthority = temp.substring(0, index2);

        return caseLocalAuthority;
    }
}
