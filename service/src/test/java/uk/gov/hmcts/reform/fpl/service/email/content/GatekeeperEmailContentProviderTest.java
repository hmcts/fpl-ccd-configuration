package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.NotifyGatekeeperTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

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
        gatekeeperNotificationTemplate.setCaseUrl(buildCaseUrl(CASE_REFERENCE));

        assertThat(gatekeeperEmailContentProvider.buildGatekeeperNotification(populatedCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualToComparingFieldByField(gatekeeperNotificationTemplate);
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
        gatekeeperNotificationTemplate.setCaseUrl(buildCaseUrl("123"));

        Map<String, Object> caseData = ImmutableMap.of(
            "orders", Orders.builder().orderType(List.of(CARE_ORDER))
                .build());

        CaseDetails caseDetails = buildCaseDetails(caseData);

        assertThat(gatekeeperEmailContentProvider.buildGatekeeperNotification(caseDetails,
            LOCAL_AUTHORITY_CODE)).isEqualToComparingFieldByField(gatekeeperNotificationTemplate);
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
        gatekeeperNotificationTemplate.setCaseUrl(buildCaseUrl("123"));

        Map<String, Object> caseData = ImmutableMap.of(
            "orders", Orders.builder().orderType(List.of(CARE_ORDER)).build(),
            "hearing", Hearing.builder()
                    .timeFrame("Two days")
                .build());

        CaseDetails caseDetails = buildCaseDetails(caseData);

        assertThat(gatekeeperEmailContentProvider.buildGatekeeperNotification(caseDetails,
            LOCAL_AUTHORITY_CODE)).isEqualToComparingFieldByField(gatekeeperNotificationTemplate);
    }

    @Test
    void shouldFormatRecipientLabelCorrectlyWhenMultipleGatekeeperEmailsArePresent() {
        List<String> gatekeeperEmails = ImmutableList.of(
            "JohnSmith@gmail.com",
            "SarahSimpson@gmail.com",
            "JohnSamuels@gmail.com"
        );

        String formattedMessage = gatekeeperEmailContentProvider.buildRecipientsLabel(gatekeeperEmails,
            "JohnSmith@gmail.com");

        assertThat(formattedMessage).isEqualTo("SarahSimpson@gmail.com, JohnSamuels@gmail.com has also received"
            + " this notification");
    }

    @Test
    void shouldReturnEmptyStringIfOnlyOneRecipient() {
        List<String> gatekeeperEmails = ImmutableList.of("JohnSmith@gmail.com");
        String formattedMessage = gatekeeperEmailContentProvider.buildRecipientsLabel(gatekeeperEmails,
            "JohnSmith@gmail.com");

        assertThat(formattedMessage).isEmpty();
    }

    private CaseDetails buildCaseDetails(Map<String, Object> data) {
        return CaseDetails.builder()
            .id(123L)
            .data(data)
            .build();
    }
}
