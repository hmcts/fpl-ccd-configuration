package uk.gov.hmcts.reform.fpl.request;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class SimpleRequestData implements RequestData {

    private final String authorisation;
    private final String userId;
    private final Set<String> userRoles;

    public SimpleRequestData(RequestData requestData) {
        this(requestData.authorisation(), requestData.userId(), requestData.userRoles());
    }

    public SimpleRequestData(String authorisation, String userId, Set<String> userRoles) {
        this.authorisation = authorisation;
        this.userId = userId;
        this.userRoles = userRoles;
    }

    @Override
    public String authorisation() {
        return authorisation;
    }

    @Override
    public String userId() {
        return userId;
    }

    @Override
    public Set<String> userRoles() {
        return defaultIfNull(userRoles, emptySet());
    }
}
