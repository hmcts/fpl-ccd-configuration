package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.orderremoval.OrderRemovalTemplate;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {OrderRemovalEmailContentProvider.class})
class OrderRemovalEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Long CASE_ID = 12345L;
    private static final String FAKE_URL = "http://fake-url/cases/case-details/12345";
    private static final String REMOVAL_REASON = "removal reason test";
    private static final CaseData CASE_DATA = mock(CaseData.class);

    @Autowired
    private OrderRemovalEmailContentProvider underTest;

    @MockBean
    private EmailNotificationHelper helper;

    @BeforeEach
    void setUp() {
        when(CASE_DATA.getId()).thenReturn(CASE_ID);

        List<Element<Child>> children = wrapElements(mock(Child.class));
        when(CASE_DATA.getAllChildren()).thenReturn(children);
        when(helper.getEldestChildLastName(children)).thenReturn("Smith");
    }

    @Test
    void shouldGetSDORemovedEmailNotificationParameters() {
        final OrderRemovalTemplate actual = underTest.buildNotificationForOrderRemoval(CASE_DATA, REMOVAL_REASON);
        final OrderRemovalTemplate expectedTemplate = expectedTemplate();

        assertThat(actual).isEqualTo(expectedTemplate);
    }

    @Test
    void shouldGetCMORemovedEmailNotificationParameters() {
        OrderRemovalTemplate actualTemplate = underTest.buildNotificationForOrderRemoval(CASE_DATA, REMOVAL_REASON);
        OrderRemovalTemplate expectedTemplate = expectedTemplate();

        assertThat(actualTemplate).isEqualTo(expectedTemplate);
    }

    private OrderRemovalTemplate expectedTemplate() {
        return OrderRemovalTemplate.builder()
            .lastName("Smith")
            .removalReason(REMOVAL_REASON)
            .caseReference(String.valueOf(CASE_ID))
            .caseUrl(FAKE_URL)
            .build();
    }

}
