package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.service.CaseUrlServiceTest.CCD_UI_URL;
import static uk.gov.hmcts.reform.fpl.service.CaseUrlServiceTest.XUI_URL;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseUrlService.class,})
@TestPropertySource(properties = {"ccd.ui.base.url=" + CCD_UI_URL, "manage-case.ui.base.url=" + XUI_URL})
class CaseUrlServiceTest {

    static final String CCD_UI_URL = "http://ccd";
    static final String XUI_URL = "http://xui";

    private final long caseId = RandomUtils.nextLong();

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CaseUrlService caseUrlService;

    @Test
    void shouldGetCaseUrlsForCcd() {
        when(featureToggleService.isExpertUIEnabled()).thenReturn(false);

        assertThat(caseUrlService.getCaseUrl(caseId))
            .isEqualTo(String.format("%s/case/PUBLICLAW/CARE_SUPERVISION_EPO/%s", CCD_UI_URL, caseId));
        assertThat(caseUrlService.getCaseUrl(caseId, "Tab1"))
            .isEqualTo(String.format("%s/case/PUBLICLAW/CARE_SUPERVISION_EPO/%s#Tab1", CCD_UI_URL, caseId));
    }

    @Test
    void shouldGetCaseUrlsForXui() {
        when(featureToggleService.isExpertUIEnabled()).thenReturn(true);

        assertThat(caseUrlService.getCaseUrl(caseId))
            .isEqualTo(String.format("%s/cases/case-details/%s", XUI_URL, caseId));
        assertThat(caseUrlService.getCaseUrl(caseId, "Tab1"))
            .isEqualTo(String.format("%s/cases/case-details/%s#Tab1", XUI_URL, caseId));
    }

}
