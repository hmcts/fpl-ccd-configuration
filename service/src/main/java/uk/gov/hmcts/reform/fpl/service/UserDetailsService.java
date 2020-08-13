package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Service
public class UserDetailsService {

    private final IdamClient idamClient;
    private final RequestData requestData;

    @Autowired
    public UserDetailsService(IdamClient idamClient, RequestData requestData) {
        this.idamClient = idamClient;
        this.requestData = requestData;
    }

    /**
     * Returns a user's full name.
     *
     * @return Users full name.
     */
    public String getUserName() {
        return idamClient.getUserInfo(requestData.authorisation()).getName();
    }
}
