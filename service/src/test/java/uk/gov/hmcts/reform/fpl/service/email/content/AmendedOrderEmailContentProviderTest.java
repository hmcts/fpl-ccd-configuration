package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.AmendedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.OrderAmendedNotifyData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {AmendedOrderEmailContentProvider.class, LookupTestConfig.class})
class AmendedOrderEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final CaseData CASE_DATA = CaseData.builder()
        .id(Long.valueOf(CASE_REFERENCE))
        .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
        .familyManCaseNumber("SACCCCCCCC5676576567")
        .orderCollection(createOrders(testDocument))
        .hearingDetails(createHearingBookings(
            LocalDateTime.now().plusMonths(3), LocalDateTime.now().plusMonths(3).plusHours(1)
        ))
        .children1(wrapElements(Child.builder()
            .party(ChildParty.builder()
                .lastName("McDonnell")
                .build())
            .build()))
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName("Jones").build())
            .build()))
        .build();

    @Autowired
    private AmendedOrderEmailContentProvider underTest;

    @MockBean
    private EmailNotificationHelper helper;

    @BeforeEach
    void setUp() {
        when(helper.getEldestChildLastName(CASE_DATA.getChildren1())).thenReturn("McDonnell");
    }

    @Test
    void shouldBuildAmendedOrderParameters() {
        NotifyData expectedParameters = getExpectedParameters();
        NotifyData actualParameters = underTest.getNotifyData(
            CASE_DATA, testDocument, AmendedOrderType.CASE_MANAGEMENT_ORDER.getLabel());

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    public OrderAmendedNotifyData getExpectedParameters() {
        return OrderAmendedNotifyData.builder()
            .orderType("case management order")
            .callout("^Jones, SACCCCCCCC5676576567, hearing 15 Oct 2021")
            .courtName("Family Court")
            .documentLink("http://fake-url/testUrl")
            .caseUrl("http://fake-url/cases/case-details/12345#Orders")
            .lastName("McDonell")
            .build();
    }
}
