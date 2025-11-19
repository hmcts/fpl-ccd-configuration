package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.RespondQueryNotifyData;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {RespondQueryContentProvider.class})
public class RespondQueryContentProviderTest extends AbstractEmailContentProviderTest {
    private static final String QUERY_DATE = "2025-06-01";
    private static final CaseData CASE_DATA = CaseData.builder().id(1L).caseName("test").build();

    @Autowired
    private RespondQueryContentProvider underTest;

    @Test
    void shouldReturnNotifyData() {
        assertThat(underTest.getRespondQueryNotifyData(CASE_DATA, QUERY_DATE))
            .isEqualTo(RespondQueryNotifyData.builder()
                .caseId(CASE_DATA.getId().toString())
                .caseName(CASE_DATA.getCaseName())
                .caseUrl(caseUrl(CASE_DATA.getId().toString()))
                .queryDate(QUERY_DATE)
                .build());
    }
}
