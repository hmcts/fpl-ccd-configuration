package uk.gov.hmcts.reform.fpl.service.robotics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.robotics.RoboticsEmailConfiguration;
import uk.gov.hmcts.reform.fpl.events.CaseNumberAdded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;
import uk.gov.hmcts.reform.fpl.service.EmailService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.service.robotics.SampleRoboticsTestDataHelper.expectedRoboticsData;
import static uk.gov.hmcts.reform.fpl.service.robotics.SampleRoboticsTestDataHelper.invalidRoboticsDataWithZeroOwningCourt;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
public class RoboticsNotificationServiceTest {
    private static final String EMAIL_RECIPIENT = "recipient@example.com";
    private static final String EMAIL_FROM = "no-reply@exaple.com";

    private static long CASE_ID = 12345L;

    private static final LocalDate NOW = LocalDate.now();

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private RoboticsEmailConfiguration roboticsEmailConfiguration;

    @Mock
    private RoboticsDataService roboticsDataService;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgumentCaptor;

    private RoboticsNotificationService roboticsNotificationService;

    @BeforeEach
    void setup() {
        given(roboticsEmailConfiguration.getRecipient())
            .willReturn(EMAIL_RECIPIENT);

        given(roboticsEmailConfiguration.getSender())
            .willReturn(EMAIL_FROM);

        roboticsNotificationService = new RoboticsNotificationService(emailService, roboticsDataService,
            roboticsEmailConfiguration, objectMapper);
    }

    @Test
    void notifyRoboticsOfSubmittedCaseDataShouldSendNotificationToRobotics() throws IOException {
        RoboticsData expectedRoboticsData = expectedRoboticsData(EMERGENCY_PROTECTION_ORDER.getLabel());
        given(roboticsDataService.prepareRoboticsData(prepareCaseData(), CASE_ID))
            .willReturn(expectedRoboticsData);

        String expectedRoboticsDataJson = objectMapper.writeValueAsString(expectedRoboticsData);
        given(roboticsDataService.convertRoboticsDataToJson(expectedRoboticsData))
            .willReturn(expectedRoboticsDataJson);

        roboticsNotificationService.notifyRoboticsOfSubmittedCaseData(new CaseNumberAdded(prepareCaseDetails()));

        verify(emailService).sendEmail(eq(EMAIL_FROM), emailDataArgumentCaptor.capture());

        assertEmailDataAndAttachedJsonData(emailDataArgumentCaptor.getValue(), expectedRoboticsDataJson);
    }

    @ParameterizedTest
    @MethodSource("blankAndNull")
    void notifyRoboticsOfSubmittedCaseDataShouldSendNotificationToRoboticsWhenApplicantContactNumberIsNullOrEmpty(
        final String number) throws IOException {
        RoboticsData expectedRoboticsData = expectedRoboticsData(EMERGENCY_PROTECTION_ORDER.getLabel(), number);
        given(roboticsDataService.prepareRoboticsData(prepareCaseData(), CASE_ID))
            .willReturn(expectedRoboticsData);

        String expectedRoboticsDataJson = objectMapper.writeValueAsString(expectedRoboticsData);
        given(roboticsDataService.convertRoboticsDataToJson(expectedRoboticsData))
            .willReturn(expectedRoboticsDataJson);

        roboticsNotificationService.notifyRoboticsOfSubmittedCaseData(new CaseNumberAdded(prepareCaseDetails()));

        verify(emailService).sendEmail(eq(EMAIL_FROM), emailDataArgumentCaptor.capture());

        assertEmailDataAndAttachedJsonData(emailDataArgumentCaptor.getValue(), expectedRoboticsDataJson);
    }

    @Test
    void resendRoboticsOfSubmittedCaseDataShouldSendNotificationToRobotics() throws IOException {
        RoboticsData expectedRoboticsData = expectedRoboticsData(EMERGENCY_PROTECTION_ORDER.getLabel());
        given(roboticsDataService.prepareRoboticsData(prepareCaseData(), CASE_ID))
            .willReturn(expectedRoboticsData);

        String expectedRoboticsDataJson = objectMapper.writeValueAsString(expectedRoboticsData);
        given(roboticsDataService.convertRoboticsDataToJson(expectedRoboticsData))
            .willReturn(expectedRoboticsDataJson);

        roboticsNotificationService.sendSubmittedCaseData(prepareCaseDetails());

        verify(emailService).sendEmail(eq(EMAIL_FROM), emailDataArgumentCaptor.capture());

        assertEmailDataAndAttachedJsonData(emailDataArgumentCaptor.getValue(), expectedRoboticsDataJson);
    }

    @Test
    void notifyRoboticsOfSubmittedCaseDataShouldNotSendEmailWhenOwningCourtCodeZero()
        throws IOException {
        CaseData caseData = prepareCaseData();

        given(roboticsDataService.prepareRoboticsData(caseData, CASE_ID))
            .willReturn(invalidRoboticsDataWithZeroOwningCourt());

        verify(emailService, never()).sendEmail(eq(EMAIL_FROM), emailDataArgumentCaptor.capture());
    }

    @Test
    void notifyRoboticsOfSubmittedCaseDataShouldNotSendEmailWhenRoboticsJsonDataNull()
        throws IOException {
        CaseData caseData = prepareCaseData();

        given(roboticsDataService.prepareRoboticsData(caseData, CASE_ID))
            .willReturn(expectedRoboticsData(EDUCATION_SUPERVISION_ORDER.getLabel()));

        verify(emailService, never()).sendEmail(eq(EMAIL_FROM), emailDataArgumentCaptor.capture());
    }

    private CaseData prepareCaseData() throws IOException {
        CaseData caseData = objectMapper.convertValue(populatedCaseDetails().getData(), CaseData.class);
        caseData.setDateSubmitted(NOW);
        return caseData;
    }

    private CaseDetails prepareCaseDetails() throws IOException {
        CaseDetails caseDetails = populatedCaseDetails();

        Map<String, Object> caseDataMap = populatedCaseDetails().getData();
        caseDataMap.put("dateSubmitted", NOW);

        return caseDetails.toBuilder()
            .data(caseDataMap)
            .build();
    }

    private void assertEmailDataAndAttachedJsonData(final EmailData capturedEmailData,
                                                    final String expectedRoboticsDataJson) {
        assertThat(capturedEmailData.getSubject()).isEqualTo("CaseSubmitted_12345");
        assertThat(capturedEmailData.getRecipient()).isEqualTo(EMAIL_RECIPIENT);
        assertThat(capturedEmailData.getAttachments()).hasSize(1);
        assertThat(capturedEmailData.getAttachments())
            .extracting("data", "filename")
            .containsExactly(tuple(new ByteArrayResource(expectedRoboticsDataJson.getBytes()),
                "CaseSubmitted_12345.json"));
    }

    private static Stream<String> blankAndNull() {
        return Stream.of("", null);
    }
}
