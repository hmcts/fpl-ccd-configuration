package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
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

        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("localAuthority", LOCAL_AUTHORITY_NAME)
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put("ordersAndDirections", ordersAndDirections)
            .put("timeFramePresent", "Yes")
            .put("timeFrameValue", "same day")
            .put("urgentHearing", "Yes")
            .put("nonUrgentHearing", "No")
            .put("firstRespondentName", "Smith")
            .put("reference", CASE_REFERENCE)
            .put("caseUrl", buildCaseUrl(CASE_REFERENCE))
            .build();

        assertThat(gatekeeperEmailContentProvider.buildGatekeeperNotification(populatedCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualTo(expectedMap);
    }

    @Test
    void shouldReturnSuccessfullyWithEmptyCaseDetails() {
        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("localAuthority", LOCAL_AUTHORITY_NAME)
            .put("dataPresent", "No")
            .put("fullStop", "Yes")
            .put("ordersAndDirections", "")
            .put("timeFramePresent", "No")
            .put("timeFrameValue", "")
            .put("urgentHearing", "No")
            .put("nonUrgentHearing", "No")
            .put("firstRespondentName", "")
            .put("reference", "123")
            .put("caseUrl", buildCaseUrl("123"))
            .build();

        assertThat(gatekeeperEmailContentProvider.buildGatekeeperNotification(emptyCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualTo(expectedMap);
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
