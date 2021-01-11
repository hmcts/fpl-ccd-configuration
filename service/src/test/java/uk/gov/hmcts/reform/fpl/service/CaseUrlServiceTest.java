package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.TabLabel.ORDERS;
import static uk.gov.hmcts.reform.fpl.service.CaseUrlServiceTest.XUI_URL;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseUrlService.class,})
@TestPropertySource(properties = {"manage-case.ui.base.url=" + XUI_URL})
class CaseUrlServiceTest {

    static final String XUI_URL = "http://xui";

    private final long caseId = RandomUtils.nextLong();

    @Autowired
    private CaseUrlService caseUrlService;

    @Test
    void shouldGetCaseUrlsForXui() {
        assertThat(caseUrlService.getBaseUrl())
            .isEqualTo(XUI_URL);
        assertThat(caseUrlService.getCaseUrl(caseId))
            .isEqualTo(String.format("%s/cases/case-details/%s", XUI_URL, caseId));
        assertThat(caseUrlService.getCaseUrl(caseId, ORDERS))
            .isEqualTo(String.format("%s/cases/case-details/%s#Orders", XUI_URL, caseId));
    }

}
