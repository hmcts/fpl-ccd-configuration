package uk.gov.hmcts.reform.fpl.service;

import com.launchdarkly.client.LDClient;
import com.launchdarkly.client.LDUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FeatureToggleService {

    private static final String LD_USER_ID = "FPLA";

    private final LDClient ldClient;

    public boolean isXeroxPrintingEnabled() {
        return ldClient.boolVariation("xerox-printing", getUser(), false);
    }

    private LDUser getUser() {
        return new LDUser.Builder(LD_USER_ID)
            .custom("timestamp", String.valueOf(System.currentTimeMillis()))
            .build();
    }
}
