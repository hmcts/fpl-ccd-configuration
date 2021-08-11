package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.notify.ApplicationRemovedNotifyData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.removeorder.RemoveApplicationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {ApplicationRemovedEmailContentProvider.class, LookupTestConfig.class,
    RemoveApplicationService.class})
class ApplicationRemovedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final String REMOVAL_REASON = "The order was removed because incorrect data was entered";
    private static final LocalDateTime REMOVAL_DATE = LocalDateTime.of(2010, 3, 20, 20, 20, 0);

    private static final CaseData CASE_DATA = CaseData.builder()
        .id(Long.valueOf(CASE_REFERENCE))
        .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
        .familyManCaseNumber("SACCCCCCCC5676576567")
        .orderCollection(createOrders(testDocument))
        .children1(wrapElements(Child.builder()
            .party(ChildParty.builder()
                .lastName("McDonnell")
                .build())
            .build()))
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName("Jones").build())
            .build()))
        .build();

    private static final AdditionalApplicationsBundle REMOVED_APPLICATION = AdditionalApplicationsBundle.builder()
        .removalReason(REMOVAL_REASON)
        .c2DocumentBundle(C2DocumentBundle.builder()
            .applicantName("Jim Byrne")
            .document(testDocumentReference("Filename"))
            .build())
        .build();

    @Autowired
    private ApplicationRemovedEmailContentProvider underTest;

    @MockBean
    private EmailNotificationHelper helper;

    @MockBean
    private Time time;

    @BeforeEach
    void setUp() {
        when(helper.getEldestChildLastName(CASE_DATA.getChildren1())).thenReturn("McDonnell");
        when(time.now()).thenReturn(REMOVAL_DATE);
    }

    @Test
    void shouldBuildApplicationRemovedParameters() {
        ApplicationRemovedNotifyData expectedParameters = getExpectedParameters();
        ApplicationRemovedNotifyData actualParameters = underTest.getNotifyData(
            CASE_DATA, REMOVED_APPLICATION);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    private ApplicationRemovedNotifyData getExpectedParameters() {
        return ApplicationRemovedNotifyData.builder()
            .caseId("12345")
            .applicantName("Jim Byrne")
            .c2Filename("Filename")
            .caseUrl("http://fake-url/cases/case-details/12345")
            .removalDate("20 March 2010 at 8:20pm")
            .reason("The order was removed because incorrect data was entered")
            .applicationFeeText("An application fee needs to be refunded.")
            .childLastName("McDonnell").build();
    }
}
