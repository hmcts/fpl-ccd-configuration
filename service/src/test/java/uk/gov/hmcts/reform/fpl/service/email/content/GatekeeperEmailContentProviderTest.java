package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingUrgencyType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.NotifyGatekeeperTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {GatekeeperEmailContentProvider.class, LookupTestConfig.class})
class GatekeeperEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final String CHILD_LAST_NAME = "Holmes";
    private static final String RESPONDENT_LAST_NAME = "Smith";

    @Autowired
    private GatekeeperEmailContentProvider underTest;

    @MockBean
    private EmailNotificationHelper helper;

    @BeforeEach
    void mocking() {
        when(helper.getEldestChildLastName(anyList())).thenReturn(CHILD_LAST_NAME);
    }

    @Test
    void shouldReturnExpectedMapWithValidCaseDetails() {
        NotifyGatekeeperTemplate gatekeeperNotificationTemplate = NotifyGatekeeperTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .dataPresent(YES.getValue())
            .fullStop(NO.getValue())
            .ordersAndDirections(List.of("Care order"))
            .timeFramePresent(YES.getValue())
            .timeFrameValue("same day")
            .urgentHearing(YES.getValue())
            .nonUrgentHearing(NO.getValue())
            .firstRespondentName(RESPONDENT_LAST_NAME)
            .reference(CASE_REFERENCE)
            .caseUrl(caseUrl(CASE_REFERENCE))
            .childLastName(CHILD_LAST_NAME)
            .build();

        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .children1(wrapElements(mock(Child.class)))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()))
            .orders(Orders.builder().orderType(List.of(CARE_ORDER)).build())
            .hearing(Hearing.builder()
                .hearingUrgencyType(HearingUrgencyType.SAME_DAY)
                .build())
            .build();

        assertThat(underTest.buildGatekeeperNotification(caseData)).isEqualTo(gatekeeperNotificationTemplate);
    }

    @Test
    void shouldSetExpectedHearingTimeFramePropertiesWhenTimeFrameNotSameDay() {
        NotifyGatekeeperTemplate gatekeeperNotificationTemplate = NotifyGatekeeperTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .dataPresent(YES.getValue())
            .fullStop(NO.getValue())
            .ordersAndDirections(List.of("Care order"))
            .timeFramePresent(YES.getValue())
            .timeFrameValue("urgent (not same day)")
            .urgentHearing(NO.getValue())
            .nonUrgentHearing(YES.getValue())
            .firstRespondentName(RESPONDENT_LAST_NAME)
            .childLastName(CHILD_LAST_NAME)
            .reference(CASE_REFERENCE)
            .caseUrl(caseUrl(CASE_REFERENCE))
            .build();

        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .children1(wrapElements(mock(Child.class)))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()))
            .orders(Orders.builder().orderType(List.of(CARE_ORDER)).build())
            .hearing(Hearing.builder()
                .hearingUrgencyType(HearingUrgencyType.URGENT)
                .build())
            .build();

        assertThat(underTest.buildGatekeeperNotification(caseData)).isEqualTo(gatekeeperNotificationTemplate);
    }

    @Test
    void shouldUseLocalAuthorityListWhenLocalAuthorityOnCaseNotSet() {
        NotifyGatekeeperTemplate gatekeeperNotificationTemplate = NotifyGatekeeperTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .dataPresent(YES.getValue())
            .fullStop(NO.getValue())
            .ordersAndDirections(List.of("Care order"))
            .timeFramePresent(YES.getValue())
            .timeFrameValue("urgent (not same day)")
            .urgentHearing(NO.getValue())
            .nonUrgentHearing(YES.getValue())
            .firstRespondentName(RESPONDENT_LAST_NAME)
            .childLastName(CHILD_LAST_NAME)
            .reference(CASE_REFERENCE)
            .caseUrl(caseUrl(CASE_REFERENCE))
            .build();

        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .name(LOCAL_AUTHORITY_NAME)
                .build()))
            .children1(wrapElements(mock(Child.class)))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()))
            .orders(Orders.builder().orderType(List.of(CARE_ORDER)).build())
            .hearing(Hearing.builder()
                .hearingUrgencyType(HearingUrgencyType.URGENT)
                .build())
            .build();

        assertThat(underTest.buildGatekeeperNotification(caseData)).isEqualTo(gatekeeperNotificationTemplate);
    }
}
