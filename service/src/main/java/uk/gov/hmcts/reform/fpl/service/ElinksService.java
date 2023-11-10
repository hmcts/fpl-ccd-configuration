package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ElinksService {

    private final FeatureToggleService featureToggleService;

    public String getElinksAcceptHeader() {
        return featureToggleService.isElinksEnabled()
            ? "application/vnd.jrd.api+json;Version=2.0"
            : "application/vnd.jrd.api+json";
    }
}
