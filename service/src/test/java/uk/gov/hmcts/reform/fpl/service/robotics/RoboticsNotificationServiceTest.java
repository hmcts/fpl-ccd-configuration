package uk.gov.hmcts.reform.fpl.service.robotics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.robotics.RoboticsEmailConfiguration;
import uk.gov.hmcts.reform.fpl.events.CaseSubmittedEvent;
import uk.gov.hmcts.reform.fpl.exceptions.OtherOrderTypeEmailNotificationException;
import uk.gov.hmcts.reform.fpl.exceptions.RoboticsDataException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;
import uk.gov.hmcts.reform.fpl.service.EmailService;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.OTHER;
import static uk.gov.hmcts.reform.fpl.service.robotics.SampleRoboticsTestDataHelper.expectedRoboticsData;
import static uk.gov.hmcts.reform.fpl.service.robotics.SampleRoboticsTestDataHelper.invalidRoboticsDataWithZeroOwningCourt;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
public class RoboticsNotificationServiceTest {
    private static final String EMAIL_TO = "recipient@example.com";
    private static final String EMAIL_FROM = "no-reply@exaple.com";

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
            .willReturn(EMAIL_TO);

        given(roboticsEmailConfiguration.getSender())
            .willReturn(EMAIL_FROM);

        roboticsNotificationService = new RoboticsNotificationService(emailService, roboticsDataService,
            roboticsEmailConfiguration);
    }

    @Test
    void notifyRoboticsOfSubmittedCaseDataShouldSendNotificationToRobotics() throws IOException {
        RoboticsData expectedRoboticsData = expectedRoboticsData(EMERGENCY_PROTECTION_ORDER.getLabel());
        given(roboticsDataService.prepareRoboticsData(prepareCaseData()))
            .willReturn(expectedRoboticsData);

        String expectedRoboticsDataJson = objectMapper.writeValueAsString(expectedRoboticsData);
        given(roboticsDataService.convertRoboticsDataToJson(expectedRoboticsData))
            .willReturn(expectedRoboticsDataJson);

        roboticsNotificationService.notifyRoboticsOfSubmittedCaseData(new CaseSubmittedEvent(prepareCaseData()));

        verify(emailService).sendEmail(eq(EMAIL_FROM), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        assertThat(capturedEmailData.getSubject()).isEqualTo("CaseSubmitted_12345");
        assertThat(capturedEmailData.getTo()).isEqualTo(EMAIL_TO);
        assertThat(capturedEmailData.getAttachments()).hasSize(1);
        assertThat(capturedEmailData.getAttachments())
            .extracting("data", "filename")
            .containsExactly(tuple(new ByteArrayResource(expectedRoboticsDataJson.getBytes()),
                "CaseSubmitted_12345.json"));
    }

    @Test
    void notifyRoboticsOfSubmittedCaseDataShouldThrowOtherOrderTypeEmailNotificationException()
        throws IOException {
        RoboticsData expectedRoboticsData = expectedRoboticsData(OTHER.getLabel());
        given(roboticsDataService.prepareRoboticsData(prepareCaseData()))
            .willReturn(expectedRoboticsData);

        assertThrows(OtherOrderTypeEmailNotificationException.class,
            () -> roboticsNotificationService.notifyRoboticsOfSubmittedCaseData(new CaseSubmittedEvent(
                prepareCaseData())));

        verify(emailService, never()).sendEmail(eq(EMAIL_FROM), emailDataArgumentCaptor.capture());
    }

    @Test
    void notifyRoboticsOfSubmittedCaseDataShouldThrowRoboticsDataExceptionWhenOwningCourtCodeZero()
        throws IOException {
        CaseData caseData = prepareCaseData();

        given(roboticsDataService.prepareRoboticsData(caseData))
            .willReturn(invalidRoboticsDataWithZeroOwningCourt());

        assertThrows(RoboticsDataException.class,
            () -> roboticsNotificationService.notifyRoboticsOfSubmittedCaseData(new CaseSubmittedEvent(caseData)));

        verify(emailService, never()).sendEmail(eq(EMAIL_FROM), emailDataArgumentCaptor.capture());
    }

    @Test
    void notifyRoboticsOfSubmittedCaseDataShouldThrowRoboticsDataExceptionWhenRoboticsJsonDataNull()
        throws IOException {
        CaseData caseData = prepareCaseData();

        given(roboticsDataService.prepareRoboticsData(caseData))
            .willReturn(expectedRoboticsData(EDUCATION_SUPERVISION_ORDER.getLabel()));

        assertThrows(RoboticsDataException.class,
            () -> roboticsNotificationService.notifyRoboticsOfSubmittedCaseData(new CaseSubmittedEvent(
                prepareCaseData())));

        verify(emailService, never()).sendEmail(eq(EMAIL_FROM), emailDataArgumentCaptor.capture());
    }

    private CaseData prepareCaseData() throws IOException {
        CaseData caseData = objectMapper.convertValue(populatedCaseDetails().getData(), CaseData.class);
        caseData.setDateSubmitted(LocalDate.now());
        return caseData;
    }
}
