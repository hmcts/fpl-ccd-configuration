package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.NotifyGatekeeperTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.List;
import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, GatekeeperEmailContentProvider.class, LookupTestConfig.class
})
class GatekeeperEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private GatekeeperEmailContentProvider gatekeeperEmailContentProvider;

    @PostConstruct
    void setField() {
        ReflectionTestUtils.setField(gatekeeperEmailContentProvider, "uiBaseUrl", BASE_URL);
    }

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
    void shouldReturnSuccessfullyWithEmptyCaseDetails() {
        NotifyGatekeeperTemplate gatekeeperNotificationTemplate = new NotifyGatekeeperTemplate();

        gatekeeperNotificationTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        gatekeeperNotificationTemplate.setDataPresent(NO.getValue());
        gatekeeperNotificationTemplate.setFullStop(YES.getValue());
        gatekeeperNotificationTemplate.setOrdersAndDirections(List.of(""));
        gatekeeperNotificationTemplate.setTimeFramePresent(NO.getValue());
        gatekeeperNotificationTemplate.setTimeFrameValue("");
        gatekeeperNotificationTemplate.setUrgentHearing(NO.getValue());
        gatekeeperNotificationTemplate.setNonUrgentHearing(NO.getValue());
        gatekeeperNotificationTemplate.setFirstRespondentName("");
        gatekeeperNotificationTemplate.setReference("123");
        gatekeeperNotificationTemplate.setCaseUrl(buildCaseUrl("123"));

        assertThat(gatekeeperEmailContentProvider.buildGatekeeperNotification(emptyCaseDetails(),
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

        assertThat(formattedMessage).isEqualTo("");
    }
}
