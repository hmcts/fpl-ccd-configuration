package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@Service
public class UserDetailsService {

    private final IdamApi idamApi;
    private final RequestData requestData;

    @Autowired
    public UserDetailsService(IdamApi idamApi, RequestData requestData) {
        this.idamApi = idamApi;
        this.requestData = requestData;
    }

    /**
     * Returns a user's full name.
     *
     * @return Users full name.
     */
    public String getUserName() {
        return idamApi.retrieveUserInfo(requestData.authorisation()).getName();
    }
}
