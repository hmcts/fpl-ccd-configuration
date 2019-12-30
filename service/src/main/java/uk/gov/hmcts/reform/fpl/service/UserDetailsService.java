package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@Service
public class UserDetailsService {

    private final IdamApi idamApi;

    @Autowired
    public UserDetailsService(IdamApi idamApi) {
        this.idamApi = idamApi;
    }

    /**
     * Returns a user's full name.
     *
     * @param authorization String authorization token.
     * @return Users full name.
     */
    public String getUserName(String authorization) {
        return idamApi.retrieveUserInfo(authorization).getName();
    }
}
