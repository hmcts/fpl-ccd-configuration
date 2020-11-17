package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.NotifyGatekeeperTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;

@ContextConfiguration(classes = {GatekeeperEmailContentProvider.class, LookupTestConfig.class})
class GatekeeperEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private GatekeeperEmailContentProvider gatekeeperEmailContentProvider;

    @Test
    void shouldReturnExpectedMapWithValidCaseDetails() {
        List<String> ordersAndDirections = List.of("Emergency protection order", "Contact with any named person");
        NotifyGatekeeperTemplate gatekeeperNotificationTemplate = new NotifyGatekeeperTemplate();

        gatekeeperNotificationTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        gatekeeperNotificationTemplate.setDataPresent(YES.getValue());
        gatekeeperNotificationTemplate.setFullStop(NO.getValue());
        gatekeeperNotificationTemplate.setOrdersAndDirections(ordersAndDirections);
        gatekeeperNotificationTemplate.setTimeFramePresent(YES.getValue());
        gatekeeperNotificationTemplate.setTimeFrameValue("same day");
        gatekeeperNotificationTemplate.setUrgentHearing(YES.getValue());
        gatekeeperNotificationTemplate.setNonUrgentHearing(NO.getValue());
        gatekeeperNotificationTemplate.setFirstRespondentName("Smith");
        gatekeeperNotificationTemplate.setReference(CASE_REFERENCE);
        gatekeeperNotificationTemplate.setCaseUrl(caseUrl(CASE_REFERENCE));

        assertThat(gatekeeperEmailContentProvider.buildGatekeeperNotification(populatedCaseData()))
            .isEqualToComparingFieldByField(gatekeeperNotificationTemplate);
    }

    @Test
    void shouldReturnSuccessfullyWithIncompleteCaseDetails() {
        NotifyGatekeeperTemplate gatekeeperNotificationTemplate = new NotifyGatekeeperTemplate();

        gatekeeperNotificationTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        gatekeeperNotificationTemplate.setDataPresent(YES.getValue());
        gatekeeperNotificationTemplate.setFullStop(NO.getValue());
        gatekeeperNotificationTemplate.setOrdersAndDirections(List.of("Care order"));
        gatekeeperNotificationTemplate.setTimeFramePresent(NO.getValue());
        gatekeeperNotificationTemplate.setTimeFrameValue("");
        gatekeeperNotificationTemplate.setUrgentHearing(NO.getValue());
        gatekeeperNotificationTemplate.setNonUrgentHearing(NO.getValue());
        gatekeeperNotificationTemplate.setFirstRespondentName("");
        gatekeeperNotificationTemplate.setReference("123");
        gatekeeperNotificationTemplate.setCaseUrl(caseUrl("123"));

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .orders(Orders.builder().orderType(List.of(CARE_ORDER)).build())
            .build();

        assertThat(gatekeeperEmailContentProvider.buildGatekeeperNotification(caseData))
            .isEqualToComparingFieldByField(gatekeeperNotificationTemplate);
    }

    @Test
    void shouldSetExpectedHearingTimeFramePropertiesWhenTimeFrameNotSameDay() {
        NotifyGatekeeperTemplate gatekeeperNotificationTemplate = new NotifyGatekeeperTemplate();

        gatekeeperNotificationTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        gatekeeperNotificationTemplate.setDataPresent(YES.getValue());
        gatekeeperNotificationTemplate.setFullStop(NO.getValue());
        gatekeeperNotificationTemplate.setOrdersAndDirections(List.of("Care order"));
        gatekeeperNotificationTemplate.setTimeFramePresent(YES.getValue());
        gatekeeperNotificationTemplate.setTimeFrameValue("two days");
        gatekeeperNotificationTemplate.setUrgentHearing(NO.getValue());
        gatekeeperNotificationTemplate.setNonUrgentHearing(YES.getValue());
        gatekeeperNotificationTemplate.setFirstRespondentName("");
        gatekeeperNotificationTemplate.setReference("123");
        gatekeeperNotificationTemplate.setCaseUrl(caseUrl("123"));

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .orders(Orders.builder().orderType(List.of(CARE_ORDER)).build())
            .hearing(Hearing.builder()
                .timeFrame("Two days")
                .build())
            .build();

        assertThat(gatekeeperEmailContentProvider.buildGatekeeperNotification(caseData))
            .isEqualToComparingFieldByField(gatekeeperNotificationTemplate);
    }
}
