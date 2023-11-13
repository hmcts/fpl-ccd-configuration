package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ElinksServiceTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Test
    void shouldGetElinksAcceptHeaderWhenEnabled() {
        when(featureToggleService.isElinksEnabled()).thenReturn(true);
        ElinksService elinksService = new ElinksService(featureToggleService);

        assertEquals("application/vnd.jrd.api+json;Version=2.0", elinksService.getElinksAcceptHeader());
    }

    @Test
    void shouldGetNonElinksAcceptHeaderWhenDisabled() {
        when(featureToggleService.isElinksEnabled()).thenReturn(false);
        ElinksService elinksService = new ElinksService(featureToggleService);

        assertEquals("application/vnd.jrd.api+json", elinksService.getElinksAcceptHeader());
    }

}
