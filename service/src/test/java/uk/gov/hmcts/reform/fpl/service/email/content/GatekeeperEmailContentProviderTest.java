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
        NotifyGatekeeperTemplate gatekeeperNotificationTemplate = NotifyGatekeeperTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .dataPresent(YES.getValue())
            .fullStop(NO.getValue())
            .ordersAndDirections(ordersAndDirections)
            .timeFramePresent(YES.getValue())
            .timeFrameValue("same day")
            .urgentHearing(YES.getValue())
            .nonUrgentHearing(NO.getValue())
            .firstRespondentName("Smith")
            .reference(CASE_REFERENCE)
            .caseUrl(caseUrl(CASE_REFERENCE))
            .build();

        assertThat(gatekeeperEmailContentProvider.buildGatekeeperNotification(populatedCaseData()))
            .usingRecursiveComparison().isEqualTo(gatekeeperNotificationTemplate);
    }

    @Test
    void shouldReturnSuccessfullyWithIncompleteCaseDetails() {
        NotifyGatekeeperTemplate gatekeeperNotificationTemplate = NotifyGatekeeperTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .dataPresent(YES.getValue())
            .fullStop(NO.getValue())
            .ordersAndDirections(List.of("Care order"))
            .timeFramePresent(NO.getValue())
            .timeFrameValue("")
            .urgentHearing(NO.getValue())
            .nonUrgentHearing(NO.getValue())
            .firstRespondentName("")
            .reference(CASE_REFERENCE)
            .caseUrl(caseUrl(CASE_REFERENCE))
            .build();

        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .orders(Orders.builder().orderType(List.of(CARE_ORDER)).build())
            .build();

        assertThat(gatekeeperEmailContentProvider.buildGatekeeperNotification(caseData))
            .usingRecursiveComparison().isEqualTo(gatekeeperNotificationTemplate);
    }

    @Test
    void shouldSetExpectedHearingTimeFramePropertiesWhenTimeFrameNotSameDay() {
        NotifyGatekeeperTemplate gatekeeperNotificationTemplate = NotifyGatekeeperTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .dataPresent(YES.getValue())
            .fullStop(NO.getValue())
            .ordersAndDirections(List.of("Care order"))
            .timeFramePresent(YES.getValue())
            .timeFrameValue("two days")
            .urgentHearing(NO.getValue())
            .nonUrgentHearing(YES.getValue())
            .firstRespondentName("")
            .reference(CASE_REFERENCE)
            .caseUrl(caseUrl(CASE_REFERENCE))
            .build();

        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .orders(Orders.builder().orderType(List.of(CARE_ORDER)).build())
            .hearing(Hearing.builder()
                .timeFrame("Two days")
                .build())
            .build();

        assertThat(gatekeeperEmailContentProvider.buildGatekeeperNotification(caseData))
            .usingRecursiveComparison().isEqualTo(gatekeeperNotificationTemplate);
    }
}
