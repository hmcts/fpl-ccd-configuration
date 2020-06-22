package uk.gov.hmcts.reform.fpl;

import uk.gov.hmcts.ccd.sdk.types.Webhook;

public class CCDConfigDevelopment extends CCDConfig {
    @Override
    public void configure() {
        super.configure();
    }

    @Override
    protected String webhookConvention(Webhook webhook, String eventId) {
        return "localhost:5050/" + eventId + "/" + webhook;
    }

    @Override
    protected String environment() {
        return "development";
    }
}
