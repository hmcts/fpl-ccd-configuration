package uk.gov.hmcts.reform.fpl.request;

public class SimpleRequestData implements RequestData {

    private final String authorisation;
    private final String userId;

    public SimpleRequestData(RequestData requestData) {
        this(requestData.authorisation(), requestData.userId());
    }

    public SimpleRequestData(String authorisation, String userId) {
        this.authorisation = authorisation;
        this.userId = userId;
    }

    @Override
    public String authorisation() {
        return authorisation;
    }

    @Override
    public String userId() {
        return userId;
    }
}
